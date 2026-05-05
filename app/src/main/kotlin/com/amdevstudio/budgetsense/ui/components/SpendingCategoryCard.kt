package com.amdevstudio.budgetsense.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.ui.util.expenseCategoryVisual
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CategoryIconSlot = 44
private const val IconToTextGap = 12

@Composable
fun SpendingCategoryCard(
    category: String,
    spent: Long,
    cap: Long?,
    transactions: List<TransactionEntity>,
    expanded: Boolean,
    onToggle: () -> Unit,
    currencyCode: String,
    hideBalance: Boolean,
    onOpenTransaction: (String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
) {
    val visual = expenseCategoryVisual(category, MaterialTheme.colorScheme.primary)
    val limit = cap?.takeIf { it > 0L }
    val hasCap = limit != null
    val ratio = if (limit != null) spent.toFloat() / limit.toFloat() else 0f
    val over = limit != null && spent > limit
    val progressVisual = when {
        !hasCap -> 1f
        over -> 1f
        else -> ratio.coerceIn(0f, 1f)
    }
    val barColor = when {
        !hasCap -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        over -> Color(0xFFE53935)
        else -> Color(0xFF26A69A)
    }
    val dateFmt = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    }
    val zone = ZoneId.systemDefault()
    val countLabel = remember(transactions.size) {
        val n = transactions.size
        if (n == 1) "1 transaction" else "$n transactions"
    }
    val textStartPadding = (CategoryIconSlot + IconToTextGap).dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(IconToTextGap.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(CategoryIconSlot.dp)
                                .clip(CircleShape)
                                .background(visual.accent.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                visual.icon,
                                contentDescription = null,
                                tint = visual.accent,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        AdaptivePlainText(
                            text = category,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            minScale = 0.78f,
                        )
                    }
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    AdaptivePlainText(
                        text = countLabel,
                        modifier = Modifier
                            .padding(start = textStartPadding)
                            .weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        minScale = 0.72f,
                    )
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .weight(0.44f)
                            .padding(start = 8.dp),
                    ) {
                        if (hasCap) {
                            AdaptiveMonospaceValue(
                                text = MoneyFormat.format(currencyCode, spent, hideBalance),
                                color = if (over) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface,
                                compact = true,
                                textAlign = TextAlign.End,
                            )
                            AdaptiveMonospaceValue(
                                text = "of ${MoneyFormat.format(currencyCode, limit, hideBalance)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                compact = true,
                                textAlign = TextAlign.End,
                                minScale = 0.5f,
                            )
                        } else {
                            AdaptiveMonospaceValue(
                                text = MoneyFormat.format(currencyCode, spent, hideBalance),
                                color = MaterialTheme.colorScheme.onSurface,
                                compact = true,
                                textAlign = TextAlign.End,
                            )
                            Text(
                                "No category limit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progressVisual },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(MaterialTheme.shapes.small),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(Modifier.height(8.dp))
                    transactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenTransaction(tx.id) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                            ) {
                                Text(
                                    tx.note.ifBlank { tx.category },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    Instant.ofEpochMilli(tx.occurredAtMillis).atZone(zone).format(dateFmt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.weight(0.46f),
                            ) {
                                AdaptiveMonospaceValue(
                                    text = "−${MoneyFormat.format(currencyCode, tx.amountCents, hideBalance)}",
                                    color = Color(0xFFE53935),
                                    compact = true,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { onDeleteTransaction(tx) }) {
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
