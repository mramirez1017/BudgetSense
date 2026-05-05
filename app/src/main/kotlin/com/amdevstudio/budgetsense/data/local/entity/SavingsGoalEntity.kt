package com.amdevstudio.budgetsense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetCents: Long,
    val savedCents: Long,
    val deadlineMillis: Long?,
)
