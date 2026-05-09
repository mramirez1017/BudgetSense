package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.roundToInt
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.domain.TipsOfTheDay
import com.amdevstudio.budgetsense.domain.Time
import com.amdevstudio.budgetsense.ui.components.BudgetSenseMonthField
import com.amdevstudio.budgetsense.ui.components.HeroSummaryCard
import com.amdevstudio.budgetsense.ui.components.MonthExpensePieChart
import com.amdevstudio.budgetsense.ui.components.expenseCategoryDashboardPalette
import com.amdevstudio.budgetsense.ui.components.NeonCalloutCard
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.TipOfTheDayDialog
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.util.appBottomBarSafePadding
import java.time.LocalDate
import java.time.ZonedDateTime

@Composable
fun DashboardScreen(
    profile: UserProfileEntity,
    monthIncome: Long,
    monthExpense: Long,
    monthBudgetCap: Long?,
    monthTransactions: List<TransactionEntity>,
    monthKey: String,
    onMonthKeyChanged: (String) -> Unit,
    savingsGoals: List<SavingsGoalEntity>,
    onOpenTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenInsights: () -> Unit,
) {
    val isCurrentMonth = remember(monthKey) { monthKey == Time.monthKey() }
    val monthLabel = remember(monthKey, isCurrentMonth) {
        if (isCurrentMonth) "This month" else Time.formatMonthKey(monthKey)
    }
    val greeting = remember {
        val hour = ZonedDateTime.now().hour
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val tipOfTheDay = remember(LocalDate.now().toEpochDay()) {
        TipsOfTheDay.forToday()
    }
    var tipDialogOpen by remember { mutableStateOf(false) }
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

    val expensePalette = expenseCategoryDashboardPalette()
    val gradientInfinite = rememberInfiniteTransition(label = "homeTileGrad")
    val tileGradientPhase by gradientInfinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tileGradientPhase",
    )

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .appBottomBarSafePadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            OverlineCaps("Home", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(
                "$greeting, ${profile.displayName}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            BudgetSenseMonthField(
                label = "Month",
                monthKey = monthKey,
                onMonthSelected = onMonthKeyChanged,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        NeonCalloutCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { tipDialogOpen = true },
            accent = MaterialTheme.colorScheme.tertiary,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        "Tip of the day",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Tap to read today’s tip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (tipDialogOpen) {
            TipOfTheDayDialog(
                description = tipOfTheDay,
                onDismiss = { tipDialogOpen = false },
            )
        }

        HeroSummaryCard(
            overline = monthLabel,
            headline = MoneyFormat.format(currency, balance, hide),
            subline = "Income: ${MoneyFormat.format(currency, monthIncome, hide)}  •  Spent: ${MoneyFormat.format(currency, monthExpense, hide)}  •  Left: ${MoneyFormat.format(currency, remaining, hide)}",
            modifier = Modifier.fillMaxWidth(),
        )

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
                    monthLabel,
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
                            containerColor = expensePalette[index % expensePalette.size],
                            gradientPhase = tileGradientPhase,
                        )
                    }
                }
            }
        }

        NeoPanel(borderAlpha = 0.28f) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Default.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    OverlineCaps("Savings", color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Goal progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            val savingsTileColors = remember {
                listOf(
                    Color(0xFF7C6FDB),
                    Color(0xFF26A69A),
                    Color(0xFF42A5F5),
                    Color(0xFFEC407A),
                    Color(0xFFFFB74D),
                )
            }
            if (savingsGoals.isEmpty()) {
                Text(
                    "No goals yet — add one in Savings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                ) {
                    itemsIndexed(
                        savingsGoals,
                        key = { _, g -> g.id },
                    ) { index, goal ->
                        SavingsGoalRingTile(
                            rank = index + 1,
                            name = goal.name,
                            savedCents = goal.savedCents,
                            targetCents = goal.targetCents,
                            currencyCode = currency,
                            hideMoney = hide,
                            containerColor = savingsTileColors[index % savingsTileColors.size],
                            gradientPhase = tileGradientPhase,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onOpenSavings, modifier = Modifier.fillMaxWidth()) {
                Text("Open Savings", color = MaterialTheme.colorScheme.primary)
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
                    sliceColors = expensePalette,
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

        TextButton(
            onClick = onOpenInsights,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open Insights", color = MaterialTheme.colorScheme.primary)
        }
    }

}

@Composable
private fun SavingsGoalRingTile(
    rank: Int,
    name: String,
    savedCents: Long,
    targetCents: Long,
    currencyCode: String,
    hideMoney: Boolean,
    containerColor: Color,
    gradientPhase: Float,
) {
    val safeTarget = targetCents.coerceAtLeast(1L)
    val ratio = (savedCents.coerceAtLeast(0L).toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)
    val pctForLabel = remember(savedCents, targetCents) {
        if (targetCents > 0L) {
            ((savedCents.coerceAtLeast(0L).toDouble() / targetCents.toDouble()) * 100).roundToInt()
        } else {
            0
        }
    }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(savedCents, targetCents) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(durationMillis = 640, easing = FastOutSlowInEasing))
    }
    val sweep = 360f * ratio * anim.value
    val trackColor = Color.White.copy(alpha = 0.32f)
    val ringColor = Color.White.copy(alpha = 0.95f)
    val savedText = MoneyFormat.format(currencyCode, savedCents.coerceAtLeast(0L), hideMoney)

    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(22.dp))
            .flowingTileGradient(containerColor, gradientPhase, cornerDp = 22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
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
            }
            Spacer(Modifier.height(6.dp))
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.size(118.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val strokePx = min(size.width, size.height) * 0.09f
                    val diameter = min(size.width, size.height) - strokePx
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)
                    val ringStroke = Stroke(width = strokePx, cap = StrokeCap.Round)
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = ringStroke,
                    )
                    if (sweep > 0.06f) {
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = ringStroke,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        savedText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            letterSpacing = 0.2.sp,
                            lineHeight = 18.sp,
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (targetCents > 0L) "$pctForLabel%" else "—",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseCategoryTile(
    rank: Int,
    category: String,
    amountText: String,
    containerColor: Color,
    gradientPhase: Float,
) {
    Box(
        modifier = Modifier
            .width(152.dp)
            .height(128.dp)
            .clip(RoundedCornerShape(22.dp))
            .flowingTileGradient(containerColor, gradientPhase, cornerDp = 22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
}

private fun Modifier.flowingTileGradient(
    containerColor: Color,
    gradientPhase: Float,
    cornerDp: Dp,
): Modifier = drawBehind {
    val w = size.width
    val h = size.height
    val p = gradientPhase
    val glow = lerp(containerColor, Color.White, 0.38f)
    val shaded = lerp(containerColor, Color.Black, 0.14f)
    val px = cornerDp.toPx()
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                lerp(containerColor, glow, 0.35f + p * 0.45f),
                containerColor,
                lerp(glow, containerColor, p * 0.7f),
                shaded,
                lerp(containerColor, glow, 0.2f + (1f - p) * 0.35f),
            ),
            start = Offset(-w * 0.15f + p * w * 0.95f, -h * 0.1f),
            end = Offset(w * 1.05f + p * w * 0.25f, h * 1.2f),
        ),
        cornerRadius = CornerRadius(px, px),
        size = size,
    )
}
