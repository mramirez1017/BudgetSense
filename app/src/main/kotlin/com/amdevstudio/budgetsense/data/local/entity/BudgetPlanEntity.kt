package com.amdevstudio.budgetsense.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_plans",
    indices = [Index(value = ["monthKey"], unique = true)],
)
data class BudgetPlanEntity(
    @PrimaryKey val monthKey: String,
    val totalBudgetCents: Long?,
    val dailyLimitCents: Long?,
    val weeklyLimitCents: Long?,
)
