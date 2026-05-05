package com.amdevstudio.budgetsense

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.amdevstudio.budgetsense.data.local.BudgetDatabase
import com.amdevstudio.budgetsense.data.repository.AuthRepository
import com.amdevstudio.budgetsense.data.repository.BillRepository
import com.amdevstudio.budgetsense.data.repository.BudgetRepository
import com.amdevstudio.budgetsense.data.repository.ProfileRepository
import com.amdevstudio.budgetsense.data.repository.SavingsRepository
import com.amdevstudio.budgetsense.data.repository.TransactionRepository
import com.amdevstudio.budgetsense.util.isNetworkLikelyAvailable
import com.amdevstudio.budgetsense.ui.BudgetSenseRoot
import com.amdevstudio.budgetsense.ui.theme.BudgetSenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val activity = context as ComponentActivity
            SideEffect {
                val w = activity.window
                val c = WindowCompat.getInsetsController(w, w.decorView)
                c.isAppearanceLightStatusBars = true
                c.isAppearanceLightNavigationBars = true
            }
            val appContext = context.applicationContext
            val database = remember { BudgetDatabase.get(context) }
            val authRepository = remember { AuthRepository() }
            val hasNetwork = remember(appContext) { { appContext.isNetworkLikelyAvailable() } }
            val txSyncPrefs = remember {
                appContext.getSharedPreferences("budgetsense_tx_sync", Context.MODE_PRIVATE)
            }
            val profileRepository = remember {
                ProfileRepository(
                    database.userProfileDao(),
                    isNetworkLikelyAvailable = hasNetwork,
                )
            }
            val transactionRepository = remember {
                TransactionRepository(
                    database.transactionDao(),
                    isNetworkLikelyAvailable = hasNetwork,
                    syncPrefs = txSyncPrefs,
                )
            }
            val budgetRepository = remember { BudgetRepository(database.budgetDao()) }
            val billRepository = remember { BillRepository(database.billDao()) }
            val savingsRepository = remember { SavingsRepository(database.savingsGoalDao()) }

            BudgetSenseTheme {
                BudgetSenseRoot(
                    authRepository = authRepository,
                    profileRepository = profileRepository,
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository,
                    billRepository = billRepository,
                    savingsRepository = savingsRepository,
                )
            }
        }
    }
}
