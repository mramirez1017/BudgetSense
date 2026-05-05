package com.amdevstudio.budgetsense.data.repository

import android.content.Context
import android.content.Intent
import com.amdevstudio.budgetsense.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    fun authState(): Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun googleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    suspend fun finishGoogleSignIn(data: Intent?): Result<Unit> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: return Result.failure(IllegalStateException("Missing ID token"))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(Exception(googleSignInMessage(e.statusCode, e.message)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun googleSignInMessage(statusCode: Int, detail: String?): String {
        val suffix = detail?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
        return when (statusCode) {
            ConnectionResult.DEVELOPER_ERROR -> {
                "Sign-in error 10 (DEVELOPER_ERROR). In Firebase Console → Project settings → Your Android app " +
                    "(package com.amdevstudio.budgetsense), add the SHA-1 (and SHA-256) for the keystore you use to " +
                    "run this build (Android Studio: Gradle → app → Tasks → android → signingReport). Save, wait a minute, then try again."
            }
            GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
                "Sign-in was cancelled."
            ConnectionResult.NETWORK_ERROR ->
                "Network error. Check your connection and try again."
            else ->
                "Google sign-in failed (code $statusCode)$suffix"
        }
    }

    suspend fun signOut(context: Context) {
        auth.signOut()
        runCatching {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, gso).signOut().await()
        }
    }
}
