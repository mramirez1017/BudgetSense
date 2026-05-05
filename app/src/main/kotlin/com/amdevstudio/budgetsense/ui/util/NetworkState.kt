package com.amdevstudio.budgetsense.ui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.amdevstudio.budgetsense.util.isNetworkLikelyAvailable

/** Observes default network so we can run Room → Firestore sync when the device comes online. */
@Composable
fun rememberNetworkAvailable(): State<Boolean> {
    val context = LocalContext.current.applicationContext
    val state = remember { mutableStateOf(context.isNetworkLikelyAvailable()) }
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                state.value = true
            }

            override fun onLost(network: Network) {
                state.value = false
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        onDispose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }
    return state
}
