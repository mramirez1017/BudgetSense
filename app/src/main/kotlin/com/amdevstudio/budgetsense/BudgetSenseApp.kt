package com.amdevstudio.budgetsense

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import com.amdevstudio.budgetsense.util.isNetworkLikelyAvailable
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings

class BudgetSenseApp : Application() {

    private val firestoreNetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runCatching { FirebaseFirestore.getInstance().enableNetwork() }
            }

            override fun onLost(network: Network) {
                // Stops watch streams that otherwise surface "client is offline" on the main thread and crash.
                runCatching { FirebaseFirestore.getInstance().disableNetwork() }
            }
        }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val firestore = FirebaseFirestore.getInstance()
        // Disk persistence + DEFAULT get() can deliver "offline" errors on snapshot listeners to the
        // main thread and crash the process. Room is the real local store; memory cache is enough for Firestore.
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()

        val cm = getSystemService(ConnectivityManager::class.java)
        if (isNetworkLikelyAvailable()) {
            runCatching { firestore.enableNetwork() }
        } else {
            runCatching { firestore.disableNetwork() }
        }
        cm.registerDefaultNetworkCallback(firestoreNetworkCallback)
    }
}
