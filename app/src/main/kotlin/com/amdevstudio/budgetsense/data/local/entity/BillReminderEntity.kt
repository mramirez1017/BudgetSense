package com.amdevstudio.budgetsense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dueAtMillis: Long,
    val repeatMonthly: Boolean,
    val notifyDaysBefore: Int,
    val lastPaidPeriod: String?,
)
