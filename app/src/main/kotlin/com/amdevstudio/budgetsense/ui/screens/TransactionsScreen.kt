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
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.SpendingCategoryCard
import com.amdevstudio.budgetsense.ui.components.futuristicFrame

private val MoneyFabReserveBottom = 120.dp

private enum class MoneyView { Overview, All }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    profile: UserProfileEntity,
    transactions: List<TransactionEntity>,
    monthBudgetCents: Long?,
    categoryCaps: Map<String, Long>,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
) {
    var view by remember { mutableStateOf(MoneyView.Overview) }
    var filter by remember { mutableStateOf<TransactionType?>(null) }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    val monthKey = remember { Time.monthKey() }
    val start = remember(monthKey) { Time.startOfMonthMillis(monthKey) }
    val end = remember(monthKey) { Time.endOfMonthMillis(monthKey) }

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
    var fabDragY by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()
        val fabPx = with(density) { 56.dp.roundToPx() }.toFloat()
        val padH = with(density) { 16.dp.toPx() }
        val padV = with(density) { 24.dp.toPx() }
        val maxDragLeft = -(maxW - fabPx - padH * 2).coerceAtLeast(0f)
        val maxDragUp = -(maxH - fabPx - padV * 2).coerceAtLeast(0f)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            OverlineCaps("Money", color = MaterialTheme.colorScheme.primary)
                            Text(
                                "This month",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    actions = {
                        ScreenHelpIconButton(title = "Money tab") {
                            Text(
                                "Overview shows this month’s income and spending, and groups expenses by category using the limits you set in Budget. Expand a category to see each purchase.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "All transactions lists every entry for the month. Use the chips to filter by income or expense. Tap + to add a new entry; you can drag the + button if it covers a row.",
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
            containerColor = Color.Transparent,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                    label = { Text("All transactions") },
                    colors = chipColors,
                )
            }

            when (view) {
                MoneyView.Overview -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = MoneyFabReserveBottom),
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                ),
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "Income",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    AdaptiveMonospaceValue(
                                        text = MoneyFormat.format(profile.currencyCode, monthIncome, profile.hideBalance),
                                        color = Color(0xFF43A047),
                                        compact = true,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                ),
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Spending", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        AdaptiveMonospaceValue(
                                            text = MoneyFormat.format(profile.currencyCode, monthExpenseTotal, profile.hideBalance),
                                            color = Color(0xFFE53935),
                                            compact = true,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.weight(1f),
                                        )
                                        if (monthBudgetCents != null && monthBudgetCents > 0L) {
                                            AdaptiveMonospaceValue(
                                                text = "of ${MoneyFormat.format(profile.currencyCode, monthBudgetCents, profile.hideBalance)}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                compact = true,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(1f),
                                                minScale = 0.5f,
                                            )
                                        }
                                    }
                                    AdaptivePlainText(
                                        text = "$expenseCount transaction${if (expenseCount == 1) "" else "s"}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Start,
                                        maxLines = 2,
                                        minScale = 0.75f,
                                    )
                                    if (monthBudgetCents != null && monthBudgetCents > 0L) {
                                        Spacer(Modifier.height(10.dp))
                                        val p = (monthExpenseTotal.toFloat() / monthBudgetCents.toFloat()).coerceIn(0f, 1.2f)
                                        LinearProgressIndicator(
                                            progress = { p.coerceAtMost(1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(999.dp)),
                                            color = if (monthExpenseTotal > monthBudgetCents) Color(0xFFE53935) else Color(0xFF26A69A),
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
                            Card(
                                onClick = { onOpen(tx.id) },
                                modifier = Modifier.futuristicFrame(
                                    MaterialTheme.shapes.large,
                                    MaterialTheme.colorScheme.primary,
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
                                                MaterialTheme.colorScheme.primary,
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
                                                maxLines = 2,
                                                minScale = 0.8f,
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
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(0.44f),
                                        ) {
                                            DataFigure(
                                                modifier = Modifier.weight(1f),
                                                text = (if (tx.type == TransactionType.INCOME) "+" else "−") +
                                                    MoneyFormat.format(
                                                        profile.currencyCode,
                                                        tx.amountCents,
                                                        profile.hideBalance,
                                                    ),
                                                compact = true,
                                                color = if (tx.type == TransactionType.INCOME) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                            )
                                            IconButton(onClick = { onDelete(tx) }) {
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
                .pointerInput(maxDragLeft, maxDragUp, fabPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        fabDragX = (fabDragX + dragAmount.x).coerceIn(maxDragLeft, 0f)
                        fabDragY = (fabDragY + dragAmount.y).coerceIn(maxDragUp, 0f)
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
