package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.domain.Time
import com.amdevstudio.budgetsense.ui.components.DataFigure
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.SpendingCategoryCard
import com.amdevstudio.budgetsense.ui.components.futuristicFrame

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        OverlineCaps("Money", color = MaterialTheme.colorScheme.primary)
                        Text("This month", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Overview matches your category limits from Budget. Expand a category for each purchase.",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Income", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        MoneyFormat.format(profile.currencyCode, monthIncome, profile.hideBalance),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF43A047),
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
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            MoneyFormat.format(profile.currencyCode, monthExpenseTotal, profile.hideBalance),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color(0xFFE53935),
                                        )
                                        if (monthBudgetCents != null && monthBudgetCents > 0L) {
                                            Text(
                                                "of ${MoneyFormat.format(profile.currencyCode, monthBudgetCents, profile.hideBalance)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Text(
                                        "$expenseCount transaction${if (expenseCount == 1) "" else "s"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(tx.category, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                tx.note.ifBlank { "No note" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Text(
                                                tx.type.name.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            DataFigure(
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
}
