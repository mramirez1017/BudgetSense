package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.domain.Insights
import com.amdevstudio.budgetsense.domain.Time
import com.amdevstudio.budgetsense.ui.components.NeonCalloutCard
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.util.appBottomBarSafePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    profile: UserProfileEntity,
    allTransactions: List<TransactionEntity>,
    categoryCaps: Map<String, Long>,
    savingsContributions: List<SavingsContributionEntity>,
    onBack: () -> Unit,
) {
    val monthKey = remember { Time.monthKey() }
    val monthStart = remember(monthKey) { Time.startOfMonthMillis(monthKey) }
    val monthEnd = remember(monthKey) { Time.endOfMonthMillis(monthKey) }
    val now = System.currentTimeMillis()
    val weekStart = Time.startOfWeekFromEpoch(now)
    val weekEnd = Time.endOfWeekFromEpoch(now)
    val lastWeekStart = weekStart - 7L * 24 * 60 * 60 * 1000
    val lastWeekEnd = weekStart

    val thisWeek = remember(allTransactions, weekStart, weekEnd) {
        allTransactions.filter {
            it.type == TransactionType.EXPENSE && it.occurredAtMillis in weekStart until weekEnd
        }
    }
    val lastWeek = remember(allTransactions, lastWeekStart, lastWeekEnd) {
        allTransactions.filter {
            it.type == TransactionType.EXPENSE && it.occurredAtMillis in lastWeekStart until lastWeekEnd
        }
    }
    val monthExpenses = remember(allTransactions, monthStart, monthEnd) {
        allTransactions.filter {
            it.type == TransactionType.EXPENSE && it.occurredAtMillis in monthStart until monthEnd
        }
    }

    val lines = remember(
        profile,
        thisWeek,
        lastWeek,
        monthExpenses,
        categoryCaps,
        savingsContributions,
    ) {
        Insights.build(
            thisWeekExpenses = thisWeek,
            lastWeekExpenses = lastWeek,
            monthExpenses = monthExpenses,
            categoryCaps = categoryCaps,
            currencyCode = profile.currencyCode,
            hideMoney = profile.hideBalance,
        ) + Insights.buildSavingsInsights(
            contributions = savingsContributions,
            currencyCode = profile.currencyCode,
            hideMoney = profile.hideBalance,
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        OverlineCaps("Insights", color = MaterialTheme.colorScheme.primary)
                        Text("Tips from your data", style = MaterialTheme.typography.titleLarge)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ScreenHelpIconButton(title = "Insights") {
                        Text(
                            "Expense tips compare this week vs last week’s spending and use category limits from Budget. Savings tips use your Savings deposits month by month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Global month selection is on Home — Money and Budget follow it. Insights spending tips follow the calendar month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .appBottomBarSafePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            lines.forEach { line ->
                NeonCalloutCard {
                    Text(
                        line,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
