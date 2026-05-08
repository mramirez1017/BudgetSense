package com.amdevstudio.budgetsense.data.repository

import android.content.SharedPreferences
import com.amdevstudio.budgetsense.data.local.dao.BillDao
import com.amdevstudio.budgetsense.data.local.entity.BillReminderEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BillRepository(
    private val dao: BillDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val isNetworkLikelyAvailable: () -> Boolean = { true },
    private val syncPrefs: SharedPreferences? = null,
) {
    private fun collection(uid: String) =
        firestore.collection("users").document(uid).collection("bill_reminders")

    private fun pendingDeleteKey(uid: String) = "pending_bill_del_$uid"

    private fun readPendingDeletes(uid: String): MutableSet<String> {
        val p = syncPrefs ?: return mutableSetOf()
        return (p.getStringSet(pendingDeleteKey(uid), null) ?: emptySet()).toMutableSet()
    }

    private fun writePendingDeletes(uid: String, ids: Set<String>) {
        val p = syncPrefs ?: return
        p.edit()
            .putStringSet(pendingDeleteKey(uid), if (ids.isEmpty()) null else HashSet(ids))
            .apply()
    }

    private fun addPendingDelete(uid: String, id: String) {
        val set = readPendingDeletes(uid)
        if (set.add(id)) writePendingDeletes(uid, set)
    }

    private fun removePendingDelete(uid: String, id: String) {
        val set = readPendingDeletes(uid)
        if (set.remove(id)) writePendingDeletes(uid, set)
    }

    fun observeAll(userId: String): Flow<List<BillReminderEntity>> = dao.observeAll(userId)

    suspend fun upsert(uid: String?, bill: BillReminderEntity) {
        val u = uid ?: return
        val scoped = bill.copy(userId = u)
        dao.insert(scoped)
        pushBill(u, scoped)
    }

    suspend fun delete(uid: String?, bill: BillReminderEntity) = withContext(Dispatchers.IO) {
        val u = uid ?: return@withContext
        if (bill.userId != u) return@withContext
        dao.delete(bill)
        if (isNetworkLikelyAvailable()) {
            runCatching { collection(u).document(bill.id).delete().await() }
        } else {
            addPendingDelete(u, bill.id)
        }
    }

    suspend fun syncWithCloud(uid: String) = withContext(Dispatchers.IO) {
        if (!isNetworkLikelyAvailable()) return@withContext
        val pendingDel = readPendingDeletes(uid)
        val snap = try {
            collection(uid).get(Source.SERVER).await()
        } catch (_: FirebaseFirestoreException) {
            return@withContext
        } catch (e: Exception) {
            if (e.message?.contains("offline", ignoreCase = true) == true) return@withContext
            return@withContext
        }

        for (doc in snap.documents) {
            if (doc.id in pendingDel) {
                runCatching { doc.reference.delete().await() }
                removePendingDelete(uid, doc.id)
                continue
            }
            val entity = doc.toBillReminderEntity(uid) ?: continue
            dao.insert(entity)
        }
        for (local in dao.getAllForUser(uid)) {
            if (!isNetworkLikelyAvailable()) break
            runCatching { collection(uid).document(local.id).set(local.toFirestoreMap()).await() }
        }
        for (id in readPendingDeletes(uid).toList()) {
            if (!isNetworkLikelyAvailable()) break
            runCatching { collection(uid).document(id).delete().await() }
            removePendingDelete(uid, id)
        }
    }

    private suspend fun pushBill(uid: String, entity: BillReminderEntity) = withContext(Dispatchers.IO) {
        if (!isNetworkLikelyAvailable()) return@withContext
        runCatching { collection(uid).document(entity.id).set(entity.toFirestoreMap()).await() }
    }
}

private fun BillReminderEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "dueAtMillis" to dueAtMillis,
    "repeatMonthly" to repeatMonthly,
    "notifyDaysBefore" to notifyDaysBefore,
    "lastPaidPeriod" to lastPaidPeriod,
)

private fun DocumentSnapshot.toBillReminderEntity(ownerUid: String): BillReminderEntity? {
    if (!exists()) return null
    return BillReminderEntity(
        id = id,
        userId = ownerUid,
        title = getString("title") ?: return null,
        dueAtMillis = getLong("dueAtMillis") ?: return null,
        repeatMonthly = getBoolean("repeatMonthly") ?: false,
        notifyDaysBefore = (getLong("notifyDaysBefore") ?: 1L).toInt(),
        lastPaidPeriod = getString("lastPaidPeriod"),
    )
}
