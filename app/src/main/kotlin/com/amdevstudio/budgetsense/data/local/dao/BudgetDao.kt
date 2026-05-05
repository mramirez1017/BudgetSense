package com.amdevstudio.budgetsense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amdevstudio.budgetsense.data.local.entity.BudgetCategoryCapEntity
import com.amdevstudio.budgetsense.data.local.entity.BudgetPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_plans WHERE monthKey = :monthKey LIMIT 1")
    fun observePlan(monthKey: String): Flow<BudgetPlanEntity?>

    @Query("SELECT * FROM budget_category_caps WHERE monthKey = :monthKey")
    fun observeCategoryCaps(monthKey: String): Flow<List<BudgetCategoryCapEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(plan: BudgetPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryCap(cap: BudgetCategoryCapEntity)

    @Query("DELETE FROM budget_category_caps WHERE monthKey = :monthKey AND category = :category")
    suspend fun deleteCategoryCap(monthKey: String, category: String)
}
