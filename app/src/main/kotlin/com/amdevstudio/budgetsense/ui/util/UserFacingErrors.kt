package com.amdevstudio.budgetsense.ui.util

import com.google.firebase.firestore.FirebaseFirestoreException

fun Throwable.isDeviceOfflineError(): Boolean {
    if (this is FirebaseFirestoreException) {
        return code == FirebaseFirestoreException.Code.UNAVAILABLE ||
            code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
            message?.contains("offline", ignoreCase = true) == true
    }
    val m = message?.lowercase().orEmpty()
    return "offline" in m ||
        "client is offline" in m ||
        "unavailable" in m ||
        "network error" in m ||
        "unreachable host" in m
}

/**
 * Avoid showing raw Firebase strings like "Failed to get document because the client is offline."
 */
fun Throwable.userFacingMessage(
    fallback: String,
    extraWhenOffline: String? = null,
): String {
    if (isDeviceOfflineError()) {
        val base = "You're offline. Check your internet connection."
        return if (extraWhenOffline != null) "$base $extraWhenOffline" else base
    }
    return message?.takeIf { it.isNotBlank() } ?: fallback
}
