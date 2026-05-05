package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.domain.SavingsMonthSnapshot
import com.amdevstudio.budgetsense.ui.components.DataFigure
import com.amdevstudio.budgetsense.ui.components.MonthExpensePieChart
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.OverlineCaps

@Composable
fun DashboardScreen(
    profile: UserProfileEntity,
    monthIncome: Long,
    monthExpense: Long,
    monthBudgetCap: Long?,
    monthTransactions: List<TransactionEntity>,
    savingsSnapshot: SavingsMonthSnapshot,
    hasSavingsGoals: Boolean,
    onOpenTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenInsights: () -> Unit,
) {
    val hide = profile.hideBalance
    val currency = profile.currencyCode
    val balance = monthIncome - monthExpense
    val cap = monthBudgetCap ?: profile.monthlyIncomeCents.takeIf { it > 0 }
    val remaining = (cap ?: 0L) - monthExpense
    val progress = if (cap != null && cap > 0) {
        (monthExpense.toFloat() / cap.toFloat()).coerceIn(0f, 1.2f)
    } else null

    val topCategories = monthTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { (_, v) -> v.sumOf { it.amountCents } }
        .entries
        .sortedByDescending { it.value }
        .take(3)

    val pieSlices = remember(monthTransactions) {
        val entries = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, v) -> v.sumOf { it.amountCents } }
            .entries
            .sortedByDescending { it.value }
        if (entries.isEmpty()) return@remember emptyList()
        val top = entries.take(7)
        val restSum = entries.drop(7).sumOf { it.value }
        buildList {
            top.forEach { add(it.key to it.value) }
            if (restSum > 0L) add("Other" to restSum)
        }
    }

    val dockShape = MaterialTheme.shapes.extraLarge
    val savingsDockShape = MaterialTheme.shapes.extraLarge
    val accentStroke = MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
    val zone = ZoneId.systemDefault()
    val savingsDateFmt = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        OverlineCaps("Home", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(
            "Hi, ${profile.displayName}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Below is this month’s snapshot. Use the bottom bar to log purchases or set a budget.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        NeoPanel(borderAlpha = 0.3f, fillAlpha = 0.72f) {
            Text(
                "What BudgetSense is for",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            val hint = MaterialTheme.colorScheme.onSurfaceVariant
            val body = MaterialTheme.typography.bodySmall
            Text("• Money — add income and expenses as they happen.", style = body, color = hint)
            Spacer(Modifier.height(6.dp))
            Text("• Budget — choose how much you want to spend this month.", style = body, color = hint)
            Spacer(Modifier.height(6.dp))
            Text("• Bills & savings — optional lists from the shortcuts below.", style = body, color = hint)
            Spacer(Modifier.height(6.dp))
            Text(
                "• Insights — plain-language tips from your real numbers.",
                style = body,
                color = hint,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Transactions save on your phone first (works offline). When you’re online, they upload to the cloud so you can recover them on another device later.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dockShape)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.06f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f),
                    ),
                    shape = dockShape,
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 520f),
                    ),
                )
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OverlineCaps(
                    "This month",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FuturisticStat(
                        "Balance",
                        MoneyFormat.format(currency, balance, hide),
                        labelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        valueColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    FuturisticStat(
                        "Income",
                        MoneyFormat.format(currency, monthIncome, hide),
                        labelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        valueColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FuturisticStat(
                        "Spent",
                        MoneyFormat.format(currency, monthExpense, hide),
                        labelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        valueColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    FuturisticStat(
                        "Left",
                        MoneyFormat.format(currency, remaining, hide),
                        labelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        valueColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(savingsDockShape)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.06f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f),
                    ),
                    shape = savingsDockShape,
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF004D40),
                            Color(0xFF00796B),
                            Color(0xFF26A69A),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(880f, 480f),
                    ),
                )
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.88f),
                    )
                    OverlineCaps(
                        "This month · savings",
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
                if (!hasSavingsGoals) {
                    Text(
                        "Create a savings goal to track deposits, targets, and this month’s progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SavingsHomeStat(
                            "Saved this month",
                            MoneyFormat.format(currency, savingsSnapshot.savedThisMonthCents, hide),
                        )
                        val pace = savingsSnapshot.suggestedMonthlyTargetCents
                        SavingsHomeStat(
                            "On-pace target",
                            if (pace != null) {
                                MoneyFormat.format(currency, pace, hide)
                            } else {
                                "—"
                            },
                        )
                    }
                    val latestLine = when {
                        savingsSnapshot.latestAmountCents != null && savingsSnapshot.latestDateMillis != null -> {
                            val d = Instant.ofEpochMilli(savingsSnapshot.latestDateMillis!!)
                                .atZone(zone)
                                .format(savingsDateFmt)
                            "${MoneyFormat.format(currency, savingsSnapshot.latestAmountCents!!, hide)} · $d"
                        }
                        else -> "No deposits yet"
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SavingsHomeStat("Latest deposit", latestLine)
                        SavingsHomeStat(
                            "Total saved",
                            MoneyFormat.format(currency, savingsSnapshot.totalSavedCents, hide),
                        )
                    }
                    Text(
                        "Combined goal target: ${MoneyFormat.format(currency, savingsSnapshot.combinedTargetCents, hide)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                    if (savingsSnapshot.suggestedMonthlyTargetCents == null) {
                        Text(
                            "Tip: add a deadline to a goal to see a suggested monthly contribution.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                }
                TextButton(onClick = onOpenSavings) {
                    Text("Open savings", color = Color.White)
                }
            }
        }

        NeoPanel(borderAlpha = 0.32f) {
            OverlineCaps("Spending mix", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                "This month — expenses by category",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            if (pieSlices.isEmpty()) {
                Text(
                    "Log expenses in Money to see your split as a chart.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MonthExpensePieChart(
                    currencyCode = currency,
                    hideMoney = hide,
                    slices = pieSlices,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        NeoPanel {
            OverlineCaps("Budget vs reality", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text("Spending compared to your plan", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    if (progress >= 0.9f) {
                        "You’re close to your monthly limit — open Budget to adjust the cap or trim spend."
                    } else {
                        "You’re within your plan so far. Keep logging so this bar stays accurate."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "Open the Budget tab and enter a monthly total. We’ll measure your real spending against it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        NeoPanel(borderAlpha = 0.28f) {
            OverlineCaps("Where money went", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text("Top expense categories (this month)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (topCategories.isEmpty()) {
                Text(
                    "No expenses logged yet — tap Money on the bar, then + to add one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                topCategories.forEach { (cat, cents) ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(cat, style = MaterialTheme.typography.titleSmall)
                        DataFigure(
                            text = MoneyFormat.format(currency, cents, hide),
                            compact = true,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        OverlineCaps("Shortcuts", color = MaterialTheme.colorScheme.onSurfaceVariant)
        val outlineBtn = BorderStroke(1.dp, accentStroke)
        val btnColors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onOpenTransactions,
                modifier = Modifier.weight(1f),
                border = outlineBtn,
                shape = MaterialTheme.shapes.medium,
                colors = btnColors,
            ) {
                Text("Money log")
            }
            OutlinedButton(
                onClick = onOpenBudget,
                modifier = Modifier.weight(1f),
                border = outlineBtn,
                shape = MaterialTheme.shapes.medium,
                colors = btnColors,
            ) {
                Text("Budget")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onOpenBills,
                modifier = Modifier.weight(1f),
                border = outlineBtn,
                shape = MaterialTheme.shapes.medium,
                colors = btnColors,
            ) { Text("Bills") }
            OutlinedButton(
                onClick = onOpenSavings,
                modifier = Modifier.weight(1f),
                border = outlineBtn,
                shape = MaterialTheme.shapes.medium,
                colors = btnColors,
            ) { Text("Savings") }
        }
        OutlinedButton(
            onClick = onOpenInsights,
            modifier = Modifier.fillMaxWidth(),
            border = outlineBtn,
            shape = MaterialTheme.shapes.medium,
            colors = btnColors,
        ) {
            Text("Insights")
        }
    }
}

@Composable
private fun RowScope.SavingsHomeStat(
    label: String,
    value: String,
) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.4.sp),
            color = Color.White.copy(alpha = 0.82f),
        )
        Spacer(Modifier.height(6.dp))
        DataFigure(text = value, color = Color.White, compact = true)
    }
}

@Composable
private fun RowScope.FuturisticStat(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.4.sp),
            color = labelColor,
        )
        Spacer(Modifier.height(6.dp))
        DataFigure(text = value, color = valueColor, compact = true)
    }
}
