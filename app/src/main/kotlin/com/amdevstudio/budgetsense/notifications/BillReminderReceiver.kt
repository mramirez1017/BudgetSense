package com.amdevstudio.budgetsense.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.amdevstudio.budgetsense.R

class BillReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Bill reminder"
        val billId = intent.getStringExtra(EXTRA_BILL_ID) ?: "bill"

        val nm = context.getSystemService(NotificationManager::class.java)
        ensureChannel(nm)

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Bill due soon")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(billId.hashCode(), notif)
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT < 26) return
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Bill reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for upcoming bill due dates"
            },
        )
    }

    companion object {
        const val CHANNEL_ID = "bill_reminders"
        const val EXTRA_BILL_ID = "bill_id"
        const val EXTRA_TITLE = "title"
    }
}

