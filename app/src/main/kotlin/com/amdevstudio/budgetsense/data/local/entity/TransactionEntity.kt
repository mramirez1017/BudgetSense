package com.amdevstudio.budgetsense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amdevstudio.budgetsense.data.local.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    /** Signed-in account id — rows are scoped so one account cannot delete another’s local data. */
    val userId: String,
    val type: TransactionType,
    val category: String,
    val amountCents: Long,
    val note: String,
    val occurredAtMillis: Long,
    val createdAtMillis: Long,
)
