package com.amdevstudio.budgetsense.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "budget_category_caps",
    primaryKeys = ["monthKey", "category"],
)
data class BudgetCategoryCapEntity(
    val monthKey: String,
    val category: String,
    val capCents: Long,
)
