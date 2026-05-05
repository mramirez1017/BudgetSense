package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.domain.SavingsMonthSnapshot
import com.amdevstudio.budgetsense.ui.components.DataFigure
import com.amdevstudio.budgetsense.ui.components.MonthExpensePieChart
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
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

    val topFiveExpenses = remember(monthTransactions) {
        monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, v) -> v.sumOf { it.amountCents } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
    }
    val topCategories = topFiveExpenses

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
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                OverlineCaps("Home", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Hi, ${profile.displayName}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            ScreenHelpIconButton(title = "Home") {
                Text(
                    "This month’s snapshot — scroll sideways on the tiles below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("• Money — add income and expenses as they happen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Budget — choose how much you want to spend this month.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Bills & savings — optional shortcuts from Home.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Insights — short tips built from your own numbers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Data is kept on your phone first (works offline). When you’re online, it syncs to your account so you can continue on another device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dockShape)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.08f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f),
                    ),
                    shape = dockShape,
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E3A5F),
                            Color(0xFF2563EB),
                            Color(0xFF0F766E),
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

        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Top expenses",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "This month",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            if (topFiveExpenses.isEmpty()) {
                Text(
                    "No expenses logged — add some in Money.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val tileColors = remember {
                    listOf(
                        Color(0xFFFF8A50),
                        Color(0xFF2DD4BF),
                        Color(0xFF60A5FA),
                        Color(0xFFA78BFA),
                        Color(0xFFFBBF24),
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                ) {
                    itemsIndexed(
                        topFiveExpenses,
                        key = { _, e -> e.key },
                    ) { index, entry ->
                        ExpenseCategoryTile(
                            rank = index + 1,
                            category = entry.key,
                            amountText = MoneyFormat.format(currency, entry.value, hide),
                            containerColor = tileColors[index % tileColors.size],
                        )
                    }
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
                    val monthlyTarget = savingsSnapshot.suggestedMonthlyTargetCents
                    val combinedTarget = savingsSnapshot.combinedTargetCents
                    val savedMonth = savingsSnapshot.savedThisMonthCents
                    val totalSaved = savingsSnapshot.totalSavedCents

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SavingsHomeStat(
                            "Saved this month",
                            MoneyFormat.format(currency, savedMonth, hide),
                        )
                        SavingsHomeStat(
                            "Target this month",
                            if (monthlyTarget != null && monthlyTarget > 0L) {
                                MoneyFormat.format(currency, monthlyTarget, hide)
                            } else {
                                "—"
                            },
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SavingsHomeStat(
                            "Total saved",
                            MoneyFormat.format(currency, totalSaved, hide),
                        )
                        SavingsHomeStat(
                            "Combined target",
                            MoneyFormat.format(currency, combinedTarget, hide),
                        )
                    }

                    val (barProgress, percentCaption) = when {
                        monthlyTarget != null && monthlyTarget > 0L -> {
                            val pct = (savedMonth * 100.0 / monthlyTarget.toDouble()).roundToInt().coerceAtLeast(0)
                            val bar = (savedMonth.toFloat() / monthlyTarget.toFloat()).coerceIn(0f, 1f)
                            bar to "$pct% of this month's target"
                        }
                        combinedTarget > 0L -> {
                            val pct = ((totalSaved * 100L) / combinedTarget).toInt().coerceIn(0, 999)
                            val bar = (totalSaved.toFloat() / combinedTarget.toFloat()).coerceIn(0f, 1f)
                            bar to "$pct% toward combined goal target"
                        }
                        else -> {
                            0f to "Add savings goals with targets to see progress"
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        percentCaption,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { barProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.28f),
                    )
                    if (monthlyTarget == null || monthlyTarget <= 0L) {
                        Text(
                            "Tip: add deadlines to your goals to get a suggested monthly savings target.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f),
                            modifier = Modifier.padding(top = 8.dp),
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
            Text("Top 5 expense categories (this month)", style = MaterialTheme.typography.titleMedium)
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
private fun ExpenseCategoryTile(
    rank: Int,
    category: String,
    amountText: String,
    containerColor: Color,
) {
    Column(
        modifier = Modifier
            .width(152.dp)
            .height(128.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.38f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                rank.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
        Column {
            Text(
                category,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                amountText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
