package com.amdevstudio.budgetsense.data.repository

import android.content.SharedPreferences
import com.amdevstudio.budgetsense.data.local.dao.SavingsGoalDao
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class SavingsRepository(
    private val dao: SavingsGoalDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val isNetworkLikelyAvailable: () -> Boolean = { true },
    private val syncPrefs: SharedPreferences? = null,
) {
    private fun goalsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("savings_goals")

    private fun contributionsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("savings_contributions")

    private fun pendingGoalDeleteKey(uid: String) = "pending_sg_del_$uid"
    private fun pendingContribDeleteKey(uid: String) = "pending_sc_del_$uid"

    private fun readPendingDeletes(key: String): MutableSet<String> {
        val p = syncPrefs ?: return mutableSetOf()
        return (p.getStringSet(key, null) ?: emptySet()).toMutableSet()
    }

    private fun writePendingDeletes(key: String, ids: Set<String>) {
        val p = syncPrefs ?: return
        p.edit().putStringSet(key, if (ids.isEmpty()) null else HashSet(ids)).apply()
    }

    private fun addPendingDelete(key: String, id: String) {
        val set = readPendingDeletes(key)
        if (set.add(id)) writePendingDeletes(key, set)
    }

    private fun removePendingDelete(key: String, id: String) {
        val set = readPendingDeletes(key)
        if (set.remove(id)) writePendingDeletes(key, set)
    }

    fun observeAll(userId: String): Flow<List<SavingsGoalEntity>> = dao.observeAll(userId)

    fun observeAllContributions(userId: String): Flow<List<SavingsContributionEntity>> =
        dao.observeAllContributions(userId)

    suspend fun upsert(uid: String?, goal: SavingsGoalEntity) {
        val u = uid ?: return
        val scoped = goal.copy(userId = u)
        if (dao.getGoalById(u, goal.id) == null) {
            dao.insertGoal(scoped)
        } else {
            dao.updateGoal(scoped)
        }
        pushGoal(u, scoped)
    }

    suspend fun delete(uid: String?, goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        val u = uid ?: return@withContext
        if (goal.userId != u) return@withContext
        dao.delete(goal)
        if (isNetworkLikelyAvailable()) {
            runCatching { goalsCollection(u).document(goal.id).delete().await() }
        } else {
            addPendingDelete(pendingGoalDeleteKey(u), goal.id)
        }
    }

    suspend fun addContribution(uid: String?, goal: SavingsGoalEntity, amountCents: Long) {
        val u = uid ?: return
        val scopedGoal = if (goal.userId == u) goal else goal.copy(userId = u)
        val contribution = SavingsContributionEntity(
            id = UUID.randomUUID().toString(),
            userId = u,
            goalId = goal.id,
            amountCents = amountCents,
            createdAtMillis = System.currentTimeMillis(),
        )
        val updated = scopedGoal.copy(savedCents = scopedGoal.savedCents + amountCents)
        dao.addContribution(contribution, updated)
        pushContribution(u, contribution)
        pushGoal(u, updated)
    }

    suspend fun removeContribution(uid: String?, goal: SavingsGoalEntity, contribution: SavingsContributionEntity) =
        withContext(Dispatchers.IO) {
            val u = uid ?: return@withContext
            if (goal.userId != u) return@withContext
            if (contribution.userId != u) return@withContext
            if (contribution.goalId != goal.id) return@withContext
            val updated = goal.copy(savedCents = (goal.savedCents - contribution.amountCents).coerceAtLeast(0L))
            dao.removeContributionAndUpdateGoal(contribution, updated)
            if (isNetworkLikelyAvailable()) {
                runCatching { contributionsCollection(u).document(contribution.id).delete().await() }
            } else {
                addPendingDelete(pendingContribDeleteKey(u), contribution.id)
            }
            pushGoal(u, updated)
    }

    suspend fun syncWithCloud(uid: String) = withContext(Dispatchers.IO) {
        if (!isNetworkLikelyAvailable()) return@withContext

        val pendingGoalDelKey = pendingGoalDeleteKey(uid)
        val pendingContribDelKey = pendingContribDeleteKey(uid)
        val pendingGoalDel = readPendingDeletes(pendingGoalDelKey)
        val pendingContribDel = readPendingDeletes(pendingContribDelKey)

        val goalsSnap = try {
            goalsCollection(uid).get(Source.SERVER).await()
        } catch (_: FirebaseFirestoreException) {
            return@withContext
        } catch (e: Exception) {
            if (e.message?.contains("offline", ignoreCase = true) == true) return@withContext
            return@withContext
        }
        val contribSnap = try {
            contributionsCollection(uid).get(Source.SERVER).await()
        } catch (_: FirebaseFirestoreException) {
            return@withContext
        } catch (e: Exception) {
            if (e.message?.contains("offline", ignoreCase = true) == true) return@withContext
            return@withContext
        }

        // Pull goals first (FK dependency)
        for (doc in goalsSnap.documents) {
            if (doc.id in pendingGoalDel) {
                runCatching { doc.reference.delete().await() }
                removePendingDelete(pendingGoalDelKey, doc.id)
                continue
            }
            val entity = doc.toSavingsGoalEntity(uid) ?: continue
            runCatching {
                if (dao.getGoalById(uid, entity.id) == null) dao.insertGoal(entity) else dao.updateGoal(entity)
            }
        }
        for (doc in contribSnap.documents) {
            if (doc.id in pendingContribDel) {
                runCatching { doc.reference.delete().await() }
                removePendingDelete(pendingContribDelKey, doc.id)
                continue
            }
            val entity = doc.toSavingsContributionEntity(uid) ?: continue
            runCatching { dao.insertContribution(entity) }
        }

        // Push locals
        for (g in dao.getAllGoalsForUser(uid)) {
            if (!isNetworkLikelyAvailable()) break
            runCatching { goalsCollection(uid).document(g.id).set(g.toFirestoreMap()).await() }
        }
        for (c in dao.getAllContributionsForUser(uid)) {
            if (!isNetworkLikelyAvailable()) break
            runCatching { contributionsCollection(uid).document(c.id).set(c.toFirestoreMap()).await() }
        }

        // Apply pending deletes
        for (id in readPendingDeletes(pendingGoalDelKey).toList()) {
            if (!isNetworkLikelyAvailable()) break
            runCatching { goalsCollection(uid).document(id).delete().await() }
            removePendingDelete(pendingGoalDelKey, id)
        }
        for (id in readPendingDeletes(pendingContribDelKey).toList()) {
            if (!isNetworkLikelyAvailable()) break
            runCatching { contributionsCollection(uid).document(id).delete().await() }
            removePendingDelete(pendingContribDelKey, id)
        }
    }

    private suspend fun pushGoal(uid: String, entity: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        if (!isNetworkLikelyAvailable()) return@withContext
        runCatching { goalsCollection(uid).document(entity.id).set(entity.toFirestoreMap()).await() }
    }

    private suspend fun pushContribution(uid: String, entity: SavingsContributionEntity) = withContext(Dispatchers.IO) {
        if (!isNetworkLikelyAvailable()) return@withContext
        runCatching { contributionsCollection(uid).document(entity.id).set(entity.toFirestoreMap()).await() }
    }
}

private fun SavingsGoalEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "targetCents" to targetCents,
    "savedCents" to savedCents,
    "deadlineMillis" to deadlineMillis,
)

private fun SavingsContributionEntity.toFirestoreMap(): Map<String, Any> = mapOf(
    "goalId" to goalId,
    "amountCents" to amountCents,
    "createdAtMillis" to createdAtMillis,
)

private fun DocumentSnapshot.toSavingsGoalEntity(ownerUid: String): SavingsGoalEntity? {
    if (!exists()) return null
    return SavingsGoalEntity(
        id = id,
        userId = ownerUid,
        name = getString("name") ?: return null,
        targetCents = getLong("targetCents") ?: return null,
        savedCents = getLong("savedCents") ?: 0L,
        deadlineMillis = getLong("deadlineMillis"),
    )
}

private fun DocumentSnapshot.toSavingsContributionEntity(ownerUid: String): SavingsContributionEntity? {
    if (!exists()) return null
    return SavingsContributionEntity(
        id = id,
        userId = ownerUid,
        goalId = getString("goalId") ?: return null,
        amountCents = getLong("amountCents") ?: return null,
        createdAtMillis = getLong("createdAtMillis") ?: return null,
    )
}
