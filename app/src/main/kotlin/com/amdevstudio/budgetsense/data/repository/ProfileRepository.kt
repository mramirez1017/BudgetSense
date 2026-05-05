package com.amdevstudio.budgetsense.data.repository

import com.amdevstudio.budgetsense.data.local.dao.UserProfileDao
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val dao: UserProfileDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val isNetworkLikelyAvailable: () -> Boolean = { true },
) {
    fun observe(uid: String): Flow<UserProfileEntity?> = dao.observe(uid)

    suspend fun getLocal(uid: String): UserProfileEntity? = dao.get(uid)

    suspend fun ensureLocalUser(uid: String, fallbackName: String) {
        var local = dao.get(uid)
        if (local == null) {
            pullRemote(uid)
            local = dao.get(uid)
        }
        if (local == null) {
            val initial = UserProfileEntity(
                userId = uid,
                displayName = fallbackName.ifBlank { "You" },
                currencyCode = "PHP",
                monthlyIncomeCents = 0L,
                onboardingComplete = false,
                hideBalance = false,
            )
            dao.upsert(initial)
            pushRemote(initial)
        }
    }

    suspend fun save(profile: UserProfileEntity) {
        dao.upsert(profile)
    }

    /** Call from `Dispatchers.IO` after UI (e.g. navigation) so Firestore cannot block the main thread. */
    suspend fun syncProfileToCloud(profile: UserProfileEntity) {
        pushRemote(profile)
    }

    private suspend fun pullRemote(uid: String) = withContext(Dispatchers.IO) {
        if (!isNetworkLikelyAvailable()) return@withContext
        try {
            // SERVER avoids DEFAULT merge behavior that can surface fatal listener errors when offline.
            val doc = firestore.collection("users").document(uid).get(Source.SERVER).await()
            if (!doc.exists()) return@withContext
            val p = doc.toProfile(uid) ?: return@withContext
            dao.upsert(p)
        } catch (_: FirebaseFirestoreException) {
            // Offline, permission, or other Firestore errors: keep using local Room only.
            return@withContext
        } catch (_: Exception) {
            return@withContext
        }
    }

    private suspend fun pushRemote(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        if (!isNetworkLikelyAvailable()) return@withContext
        try {
            firestore.collection("users").document(profile.userId)
                .set(profile.toFirestoreMap(), SetOptions.merge())
                .await()
        } catch (_: FirebaseFirestoreException) {
            return@withContext
        } catch (_: Exception) {
            return@withContext
        }
    }
}

private fun UserProfileEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "displayName" to displayName,
    "currencyCode" to currencyCode,
    "monthlyIncomeCents" to monthlyIncomeCents,
    "onboardingComplete" to onboardingComplete,
    "hideBalance" to hideBalance,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toProfile(uid: String): UserProfileEntity? {
    if (!exists()) return null
    return UserProfileEntity(
        userId = uid,
        displayName = getString("displayName") ?: "You",
        currencyCode = getString("currencyCode") ?: "PHP",
        monthlyIncomeCents = getLong("monthlyIncomeCents") ?: 0L,
        onboardingComplete = getBoolean("onboardingComplete") ?: false,
        hideBalance = getBoolean("hideBalance") ?: false,
    )
}
