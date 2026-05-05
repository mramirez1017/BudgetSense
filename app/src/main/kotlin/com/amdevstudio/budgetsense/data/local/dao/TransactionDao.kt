package com.amdevstudio.budgetsense.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        "SELECT * FROM transactions WHERE userId = :userId AND occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis ORDER BY occurredAtMillis DESC, createdAtMillis DESC",
    )
    fun observeInRange(userId: String, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE userId = :userId ORDER BY occurredAtMillis DESC, createdAtMillis DESC",
    )
    fun observeAll(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getAllForUser(userId: String): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query(
        "SELECT * FROM transactions WHERE userId = :userId AND type = :filterType AND category = :category AND occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis ORDER BY occurredAtMillis DESC",
    )
    fun observeByTypeAndCategory(
        userId: String,
        filterType: TransactionType,
        category: String,
        startMillis: Long,
        endMillis: Long,
    ): Flow<List<TransactionEntity>>

    @Query(
        "SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE userId = :userId AND type = 'INCOME' AND occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis",
    )
    fun observeIncomeTotal(userId: String, startMillis: Long, endMillis: Long): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis",
    )
    fun observeExpenseTotal(userId: String, startMillis: Long, endMillis: Long): Flow<Long>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getByIdForUser(userId: String, id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)
}
