package com.amdevstudio.budgetsense.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.amdevstudio.budgetsense.data.local.dao.BillDao
import com.amdevstudio.budgetsense.data.local.dao.BudgetDao
import com.amdevstudio.budgetsense.data.local.dao.SavingsGoalDao
import com.amdevstudio.budgetsense.data.local.dao.TransactionDao
import com.amdevstudio.budgetsense.data.local.dao.UserProfileDao
import com.amdevstudio.budgetsense.data.local.entity.BillReminderEntity
import com.amdevstudio.budgetsense.data.local.entity.BudgetCategoryCapEntity
import com.amdevstudio.budgetsense.data.local.entity.BudgetPlanEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity

@Database(
    entities = [
        TransactionEntity::class,
        UserProfileEntity::class,
        BudgetPlanEntity::class,
        BudgetCategoryCapEntity::class,
        BillReminderEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun budgetDao(): BudgetDao
    abstract fun billDao(): BillDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var instance: BudgetDatabase? = null

        fun get(context: Context): BudgetDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budgetsense.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
