package com.amdevstudio.budgetsense.data.repository

import com.amdevstudio.budgetsense.data.local.dao.SavingsGoalDao
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SavingsRepository(
    private val dao: SavingsGoalDao,
) {
    fun observeAll(): Flow<List<SavingsGoalEntity>> = dao.observeAll()

    fun observeAllContributions(): Flow<List<SavingsContributionEntity>> = dao.observeAllContributions()

    suspend fun upsert(goal: SavingsGoalEntity) {
        if (dao.getGoalById(goal.id) == null) {
            dao.insertGoal(goal)
        } else {
            dao.updateGoal(goal)
        }
    }

    suspend fun delete(goal: SavingsGoalEntity) = dao.delete(goal)

    suspend fun addContribution(goal: SavingsGoalEntity, amountCents: Long) {
        val contribution = SavingsContributionEntity(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            amountCents = amountCents,
            createdAtMillis = System.currentTimeMillis(),
        )
        val updated = goal.copy(savedCents = goal.savedCents + amountCents)
        dao.addContribution(contribution, updated)
    }

    suspend fun removeContribution(goal: SavingsGoalEntity, contribution: SavingsContributionEntity) {
        if (contribution.goalId != goal.id) return
        val updated = goal.copy(savedCents = (goal.savedCents - contribution.amountCents).coerceAtLeast(0L))
        dao.removeContributionAndUpdateGoal(contribution, updated)
    }
}
