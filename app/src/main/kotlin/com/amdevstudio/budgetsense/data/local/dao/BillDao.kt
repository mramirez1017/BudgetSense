package com.amdevstudio.budgetsense.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amdevstudio.budgetsense.data.local.entity.BillReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY dueAtMillis ASC")
    fun observeAll(userId: String): Flow<List<BillReminderEntity>>

    @Query("SELECT * FROM bills WHERE userId = :userId")
    suspend fun getAllForUser(userId: String): List<BillReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillReminderEntity)

    @Update
    suspend fun update(bill: BillReminderEntity)

    @Delete
    suspend fun delete(bill: BillReminderEntity)
}
