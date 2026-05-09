package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.domain.Time
import com.amdevstudio.budgetsense.ui.components.AdaptiveMonospaceValue
import com.amdevstudio.budgetsense.ui.components.AdaptivePlainText
import com.amdevstudio.budgetsense.ui.components.DataFigure
import com.amdevstudio.budgetsense.ui.components.GlassCard
import com.amdevstudio.budgetsense.ui.components.SectionHeader
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.SpendingCategoryCard
import com.amdevstudio.budgetsense.ui.components.futuristicFrame
import com.amdevstudio.budgetsense.ui.util.fabMaxDragDownPx

// Match EdgeToEdge “pill” reserve; FAB sits just above nav so list bottom padding can be tighter.
private val MoneyFabReserveBottom = 96.dp
private val IncomeGreen = Color(0xFF43A047)
private val ExpenseRed = Color(0xFFE53935)

private enum class MoneyView { Overview, All }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    profile: UserProfileEntity,
    transactions: List<TransactionEntity>,
    monthBudgetCents: Long?,
    categoryCaps: Map<String, Long>,
    monthKey: String,
    onMonthKeyChanged: (String) -> Unit,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
) {
    var view by remember { mutableStateOf(MoneyView.Overview) }
    var filter by remember { mutableStateOf<TransactionType?>(null) }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    val start = remember(monthKey) { Time.startOfMonthMillis(monthKey) }
    val end = remember(monthKey) { Time.endOfMonthMillis(monthKey) }
    val monthLabel = remember(monthKey) {
        if (monthKey == Time.monthKey()) "This month" else Time.formatMonthKey(monthKey)
    }

    val monthIncome = remember(transactions, start, end) {
        transactions
            .filter { it.type == TransactionType.INCOME && it.occurredAtMillis in start until end }
            .sumOf { it.amountCents }
    }
    val monthExpenseTotal = remember(transactions, start, end) {
        transactions
            .filter { it.type == TransactionType.EXPENSE && it.occurredAtMillis in start until end }
            .sumOf { it.amountCents }
    }
    val expenseCount = remember(transactions, start, end) {
        transactions.count { it.type == TransactionType.EXPENSE && it.occurredAtMillis in start until end }
    }

    val expenseByCategory = remember(transactions, start, end) {
        transactions
            .filter { it.type == TransactionType.EXPENSE && it.occurredAtMillis in start until end }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sortedByDescending { it.occurredAtMillis } }
    }
    val expenseTotals = remember(expenseByCategory) {
        expenseByCategory.mapValues { (_, list) -> list.sumOf { it.amountCents } }
    }
    val overviewCategories = remember(expenseTotals, categoryCaps) {
        val names = (expenseTotals.keys + categoryCaps.keys).distinct().filter { name ->
            (expenseTotals[name] ?: 0L) > 0L || (categoryCaps[name] ?: 0L) > 0L
        }
        names.sortedByDescending { expenseTotals[it] ?: 0L }
    }

    val list = remember(transactions, filter, start, end) {
        transactions
            .filter { it.occurredAtMillis in start until end }
            .filter { filter == null || it.type == filter }
            .sortedByDescending { it.occurredAtMillis }
    }

    val density = LocalDensity.current
    var fabDragX by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()
        val fabPx = with(density) { 56.dp.roundToPx() }.toFloat()
        val padH = with(density) { 16.dp.toPx() }
        val padV = with(density) { 24.dp.toPx() }
        val maxDragLeft = -(maxW - fabPx - padH * 2).coerceAtLeast(0f)
        val maxDragUp = -(maxH - fabPx - padV * 2).coerceAtLeast(0f)
        val maxDragDown = fabMaxDragDownPx(density)
        /** Same 96.dp as PillBottomBarReserve in EdgeToEdge; keeps default FAB above floating nav */
        val fabDefaultLiftPx = with(density) { 96.dp.toPx() }
        val gapAbovePillPx = with(density) { 8.dp.toPx() }
        val fabDefaultY = remember(maxDragUp, maxDragDown, fabDefaultLiftPx, gapAbovePillPx) {
            (-(fabDefaultLiftPx + gapAbovePillPx)).coerceIn(maxDragUp, maxDragDown)
        }
        var fabDragY by remember(fabDefaultY) { mutableFloatStateOf(fabDefaultY) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            OverlineCaps("Money", color = MaterialTheme.colorScheme.primary)
                            Text(
                                monthLabel,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp),
                cornerRadius = 26.dp,
                padding = 14.dp,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val chipColors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FilterChip(
                        selected = view == MoneyView.Overview,
                        onClick = { view = MoneyView.Overview },
                        label = { Text("Overview") },
                        colors = chipColors,
                    )
                    FilterChip(
                        selected = view == MoneyView.All,
                        onClick = { view = MoneyView.All },
                        label = { Text("All") },
                        colors = chipColors,
                    )
                }
            }

            when (view) {
                MoneyView.Overview -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = MoneyFabReserveBottom),
                    ) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth(), accent = IncomeGreen) {
                                SectionHeader(title = "Income") {
                                    AdaptiveMonospaceValue(
                                        text = MoneyFormat.format(profile.currencyCode, monthIncome, profile.hideBalance),
                                        color = IncomeGreen,
                                        compact = false,
                                        textAlign = TextAlign.End,
                                    )
                                }
                            }
                        }
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth(), accent = ExpenseRed) {
                                Column {
                                    SectionHeader(title = "Spending") {
                                        AdaptiveMonospaceValue(
                                            text = MoneyFormat.format(profile.currencyCode, monthExpenseTotal, profile.hideBalance),
                                            color = ExpenseRed,
                                            compact = false,
                                            textAlign = TextAlign.End,
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    AdaptivePlainText(
                                        text = "$expenseCount transaction${if (expenseCount == 1) "" else "s"}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Start,
                                        maxLines = 2,
                                        minScale = 0.85f,
                                    )
                                    if (monthBudgetCents != null && monthBudgetCents > 0L) {
                                        Spacer(Modifier.height(10.dp))
                                        val p = (monthExpenseTotal.toFloat() / monthBudgetCents.toFloat()).coerceIn(0f, 1.2f)
                                        LinearProgressIndicator(
                                            progress = { p.coerceAtMost(1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(999.dp)),
                                            color = if (monthExpenseTotal > monthBudgetCents) ExpenseRed else MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        )
                                    }
                                }
                            }
                        }
                        if (overviewCategories.isEmpty()) {
                            item {
                                Text(
                                    "No spending this month yet. Add expenses with + or set category limits in Budget.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp),
                                )
                            }
                        } else {
                            items(overviewCategories, key = { it }) { cat ->
                                val spent = expenseTotals[cat] ?: 0L
                                val txs = expenseByCategory[cat].orEmpty()
                                val cap = categoryCaps[cat]
                                SpendingCategoryCard(
                                    category = cat,
                                    spent = spent,
                                    cap = cap,
                                    transactions = txs,
                                    expanded = expandedCategories.contains(cat),
                                    onToggle = {
                                        expandedCategories = if (expandedCategories.contains(cat)) {
                                            expandedCategories - cat
                                        } else {
                                            expandedCategories + cat
                                        }
                                    },
                                    currencyCode = profile.currencyCode,
                                    hideBalance = profile.hideBalance,
                                    onOpenTransaction = onOpen,
                                    onDeleteTransaction = onDelete,
                                )
                            }
                        }
                    }
                }

                MoneyView.All -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        val chipColors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FilterChip(
                            selected = filter == null,
                            onClick = { filter = null },
                            label = { Text("All") },
                            colors = chipColors,
                        )
                        FilterChip(
                            selected = filter == TransactionType.INCOME,
                            onClick = { filter = TransactionType.INCOME },
                            label = { Text("Income") },
                            colors = chipColors,
                        )
                        FilterChip(
                            selected = filter == TransactionType.EXPENSE,
                            onClick = { filter = TransactionType.EXPENSE },
                            label = { Text("Expense") },
                            colors = chipColors,
                        )
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = MoneyFabReserveBottom),
                    ) {
                        items(list, key = { it.id }) { tx ->
                            val accent = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                            Card(
                                onClick = { onOpen(tx.id) },
                                modifier = Modifier.futuristicFrame(
                                    MaterialTheme.shapes.large,
                                    accent,
                                    0.2f,
                                ),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier
                                            .width(4.dp)
                                            .height(64.dp)
                                            .background(
                                                accent,
                                                RoundedCornerShape(
                                                    topStart = 4.dp,
                                                    bottomStart = 4.dp,
                                                    topEnd = 0.dp,
                                                    bottomEnd = 0.dp,
                                                ),
                                            ),
                                    )
                                    Row(
                                        Modifier
                                            .weight(1f)
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                        ) {
                                            AdaptivePlainText(
                                                text = tx.category,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                minScale = 0.78f,
                                            )
                                            AdaptivePlainText(
                                                text = tx.note.ifBlank { "No note" },
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Start,
                                                maxLines = 4,
                                                minScale = 0.9f,
                                            )
                                            AdaptivePlainText(
                                                text = tx.type.name.uppercase(),
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelSmall,
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                minScale = 0.85f,
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(0.6f),
                                        ) {
                                            DataFigure(
                                                text = (if (tx.type == TransactionType.INCOME) "+" else "−") +
                                                    MoneyFormat.format(
                                                        profile.currencyCode,
                                                        tx.amountCents,
                                                        profile.hideBalance,
                                                    ),
                                                compact = false,
                                                color = accent,
                                                modifier = Modifier.weight(1f),
                                            )
                                            IconButton(
                                                onClick = { onDelete(tx) },
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 24.dp)
                .offset {
                    IntOffset(fabDragX.roundToInt(), fabDragY.roundToInt())
                }
                .pointerInput(maxDragLeft, maxDragUp, maxDragDown, fabPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        fabDragX = (fabDragX + dragAmount.x).coerceIn(maxDragLeft, 0f)
                        fabDragY = (fabDragY + dragAmount.y).coerceIn(maxDragUp, maxDragDown)
                    }
                },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add transaction — drag to move")
        }
    }
}
