package com.amdevstudio.budgetsense.data.repository

import android.content.SharedPreferences
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.dao.TransactionDao
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.domain.Time
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Offline-first: every change is written to Room immediately.
 * When the network is available, rows are mirrored to `users/{uid}/transactions/{txId}` in Firestore.
 * [syncWithCloud] pulls remote changes, pushes locals, and applies any deletes that were made while offline.
 * Local rows are scoped by [TransactionEntity.userId] so account A never sees or deletes account B’s cache.
 */
class TransactionRepository(
    private val dao: TransactionDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val isNetworkLikelyAvailable: () -> Boolean = { true },
    private val syncPrefs: SharedPreferences? = null,
) {
    private fun collection(uid: String) =
        firestore.collection("users").document(uid).collection("transactions")

    private fun pendingDeleteKey(uid: String) = "pending_tx_del_$uid"

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

    fun observeMonth(monthKey: String, userId: String): Flow<List<TransactionEntity>> {
        val start = Time.startOfMonthMillis(monthKey)
        val end = Time.endOfMonthMillis(monthKey)
        return dao.observeInRange(userId, start, end)
    }

    fun observeMonthTotals(monthKey: String, userId: String): Flow<Pair<Long, Long>> {
        val start = Time.startOfMonthMillis(monthKey)
        val end = Time.endOfMonthMillis(monthKey)
        return combine(
            dao.observeIncomeTotal(userId, start, end),
            dao.observeExpenseTotal(userId, start, end),
        ) { income, expense -> income to expense }
    }

    fun observeAll(userId: String): Flow<List<TransactionEntity>> = dao.observeAll(userId)

    fun search(
        query: String,
        type: TransactionType?,
        monthKey: String?,
        userId: String,
    ): Flow<List<TransactionEntity>> {
        val q = query.trim().lowercase()
        return dao.observeAll(userId).map { list ->
            list.filter { tx ->
                val inMonth = monthKey?.let { mk ->
                    val s = Time.startOfMonthMillis(mk)
                    val e = Time.endOfMonthMillis(mk)
                    tx.occurredAtMillis in s until e
                } ?: true
                val typeOk = type == null || tx.type == type
                val textOk = q.isEmpty() ||
                    tx.note.lowercase().contains(q) ||
                    tx.category.lowercase().contains(q)
                inMonth && typeOk && textOk
            }
        }
    }

    /**
     * Pull remote documents into Room, push every local row, then apply queued Firestore deletes.
     */
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
            val entity = doc.toTransactionEntity(uid) ?: continue
            dao.insert(entity)
        }
        for (local in dao.getAllForUser(uid)) {
            if (!isNetworkLikelyAvailable()) break
            runCatching {
                collection(uid).document(local.id).set(local.toFirestoreMap()).await()
            }
        }
        for (id in readPendingDeletes(uid).toList()) {
            if (!isNetworkLikelyAvailable()) break
            runCatching { collection(uid).document(id).delete().await() }
            removePendingDelete(uid, id)
        }
    }

    suspend fun clearAllLocal(userId: String? = null) = withContext(Dispatchers.IO) {
        if (userId != null) {
            dao.deleteAllForUser(userId)
            syncPrefs?.edit()?.remove(pendingDeleteKey(userId))?.apply()
        } else {
            dao.deleteAll()
        }
    }

    suspend fun upsert(
        uid: String?,
        id: String?,
        type: TransactionType,
        category: String,
        amountCents: Long,
        note: String,
        occurredAtMillis: Long,
    ) {
        val u = uid ?: return
        val now = System.currentTimeMillis()
        val existing = id?.let { dao.getByIdForUser(u, it) }
        val entity = TransactionEntity(
            id = id ?: UUID.randomUUID().toString(),
            userId = u,
            type = type,
            category = category,
            amountCents = amountCents,
            note = note,
            occurredAtMillis = occurredAtMillis,
            createdAtMillis = existing?.createdAtMillis ?: now,
        )
        dao.insert(entity)
        pushTransaction(u, entity)
    }

    suspend fun delete(uid: String?, tx: TransactionEntity) = withContext(Dispatchers.IO) {
        val u = uid ?: return@withContext
        if (tx.userId != u) return@withContext
        dao.delete(tx)
        if (isNetworkLikelyAvailable()) {
            runCatching { collection(u).document(tx.id).delete().await() }
        } else {
            addPendingDelete(u, tx.id)
        }
    }

    suspend fun get(userId: String, id: String): TransactionEntity? = dao.getByIdForUser(userId, id)

    private suspend fun pushTransaction(uid: String, entity: TransactionEntity) =
        withContext(Dispatchers.IO) {
            if (!isNetworkLikelyAvailable()) return@withContext
            runCatching {
                collection(uid).document(entity.id).set(entity.toFirestoreMap()).await()
            }
        }
}

private fun TransactionEntity.toFirestoreMap(): Map<String, Any> = mapOf(
    "type" to type.name,
    "category" to category,
    "amountCents" to amountCents,
    "note" to note,
    "occurredAtMillis" to occurredAtMillis,
    "createdAtMillis" to createdAtMillis,
)

private fun DocumentSnapshot.toTransactionEntity(ownerUid: String): TransactionEntity? {
    if (!exists()) return null
    val typeName = getString("type") ?: return null
    val type = runCatching { TransactionType.valueOf(typeName) }.getOrNull() ?: return null
    return TransactionEntity(
        id = id,
        userId = ownerUid,
        type = type,
        category = getString("category") ?: return null,
        amountCents = getLong("amountCents") ?: return null,
        note = getString("note") ?: "",
        occurredAtMillis = getLong("occurredAtMillis") ?: return null,
        createdAtMillis = getLong("createdAtMillis") ?: System.currentTimeMillis(),
    )
}
