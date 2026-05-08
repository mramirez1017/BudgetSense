package com.amdevstudio.budgetsense.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_contributions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goalId")],
)
data class SavingsContributionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val goalId: String,
    val amountCents: Long,
    val createdAtMillis: Long,
)
