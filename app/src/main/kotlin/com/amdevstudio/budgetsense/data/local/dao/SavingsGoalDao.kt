package com.amdevstudio.budgetsense.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY deadlineMillis ASC")
    fun observeAll(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: String): SavingsGoalEntity?

    /**
     * Insert a brand-new goal only.
     * Never use [OnConflictStrategy.REPLACE] here: SQLite implements REPLACE as DELETE+INSERT on the
     * primary key, which triggers ON DELETE CASCADE on [SavingsContributionEntity] and wipes deposits.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGoal(goal: SavingsGoalEntity)

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)

    @Query("SELECT * FROM savings_contributions ORDER BY createdAtMillis DESC")
    fun observeAllContributions(): Flow<List<SavingsContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(entity: SavingsContributionEntity)

    @Transaction
    suspend fun addContribution(contribution: SavingsContributionEntity, updatedGoal: SavingsGoalEntity) {
        insertContribution(contribution)
        updateGoal(updatedGoal)
    }

    @Delete
    suspend fun deleteContributionRow(entity: SavingsContributionEntity)

    @Transaction
    suspend fun removeContributionAndUpdateGoal(contribution: SavingsContributionEntity, updatedGoal: SavingsGoalEntity) {
        deleteContributionRow(contribution)
        updateGoal(updatedGoal)
    }
}
