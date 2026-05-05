package com.amdevstudio.budgetsense.data.repository

import com.amdevstudio.budgetsense.data.local.dao.BudgetDao
import com.amdevstudio.budgetsense.data.local.entity.BudgetCategoryCapEntity
import com.amdevstudio.budgetsense.data.local.entity.BudgetPlanEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val dao: BudgetDao,
) {
    fun observePlan(monthKey: String): Flow<BudgetPlanEntity?> = dao.observePlan(monthKey)

    fun observeCategoryCaps(monthKey: String): Flow<List<BudgetCategoryCapEntity>> =
        dao.observeCategoryCaps(monthKey)

    suspend fun savePlan(plan: BudgetPlanEntity) = dao.upsertPlan(plan)

    suspend fun saveCategoryCap(cap: BudgetCategoryCapEntity) = dao.upsertCategoryCap(cap)

    suspend fun deleteCategoryCap(monthKey: String, category: String) =
        dao.deleteCategoryCap(monthKey, category)
}
