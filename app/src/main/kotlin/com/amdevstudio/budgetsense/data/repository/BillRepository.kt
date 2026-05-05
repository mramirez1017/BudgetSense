package com.amdevstudio.budgetsense.data.repository

import com.amdevstudio.budgetsense.data.local.dao.BillDao
import com.amdevstudio.budgetsense.data.local.entity.BillReminderEntity
import kotlinx.coroutines.flow.Flow

class BillRepository(
    private val dao: BillDao,
) {
    fun observeAll(): Flow<List<BillReminderEntity>> = dao.observeAll()

    suspend fun upsert(bill: BillReminderEntity) = dao.insert(bill)

    suspend fun delete(bill: BillReminderEntity) = dao.delete(bill)
}
