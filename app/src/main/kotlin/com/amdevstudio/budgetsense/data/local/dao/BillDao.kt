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
    @Query("SELECT * FROM bills ORDER BY dueAtMillis ASC")
    fun observeAll(): Flow<List<BillReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillReminderEntity)

    @Update
    suspend fun update(bill: BillReminderEntity)

    @Delete
    suspend fun delete(bill: BillReminderEntity)
}
