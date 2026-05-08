package com.amdevstudio.budgetsense.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.math.max

object BillReminderScheduler {
    fun schedule(context: Context, billId: String, title: String, dueAtMillis: Long, notifyDaysBefore: Int) {
        val msBefore = notifyDaysBefore.toLong() * 24L * 60L * 60L * 1000L
        val triggerAt = max(0L, dueAtMillis - msBefore)
        val am = context.getSystemService(AlarmManager::class.java)

        val pi = pendingIntent(context, billId, title)!!
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun cancel(context: Context, billId: String) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(context, billId, title = null, flags = PendingIntent.FLAG_NO_CREATE)
        pi?.let { am.cancel(it) }
    }

    private fun pendingIntent(
        context: Context,
        billId: String,
        title: String?,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    ): PendingIntent? {
        val intent = Intent(context, BillReminderReceiver::class.java).apply {
            putExtra(BillReminderReceiver.EXTRA_BILL_ID, billId)
            if (title != null) putExtra(BillReminderReceiver.EXTRA_TITLE, title)
        }
        val f = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            billId.hashCode(),
            intent,
            f,
        )
    }
}

