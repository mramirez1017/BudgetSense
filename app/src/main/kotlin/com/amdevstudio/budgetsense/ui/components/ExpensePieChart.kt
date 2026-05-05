package com.amdevstudio.budgetsense.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.domain.MoneyFormat
import kotlin.math.min

/**
 * Animated donut chart for expense breakdown. [slices] are category name to cents.
 */
@Composable
fun MonthExpensePieChart(
    currencyCode: String,
    hideMoney: Boolean,
    slices: List<Pair<String, Long>>,
    modifier: Modifier = Modifier,
) {
    val total = slices.sumOf { it.second }.coerceAtLeast(1L).toFloat()
    val palette = chartPalette()
    val holeColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val sliceKey = remember(slices) { slices.joinToString("|") { "${it.first}:${it.second}" } }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(sliceKey) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(durationMillis = 880, easing = FastOutSlowInEasing))
    }
    val progress = anim.value

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier.size(200.dp),
        ) {
            val stroke = min(size.width, size.height) * 0.12f
            val diameter = min(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            var start = -90f
            slices.forEachIndexed { index, (_, cents) ->
                val sweep = (cents / total) * 360f * progress
                if (sweep > 0.05f) {
                    drawArc(
                        color = palette[index % palette.size],
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize,
                    )
                    start += sweep
                }
            }
            drawCircle(
                color = holeColor,
                radius = (diameter / 2f) - stroke * 0.85f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }

        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            slices.forEachIndexed { index, (name, cents) ->
                val pct = (cents * 100f / total).coerceIn(0f, 100f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Canvas(Modifier.size(10.dp)) {
                            drawCircle(color = palette[index % palette.size])
                        }
                        Text(
                            name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        "${pct.toInt()}% · ${MoneyFormat.format(currencyCode, cents, hideMoney)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun chartPalette(): List<Color> {
    val scheme = MaterialTheme.colorScheme
    return listOf(
        scheme.primary,
        scheme.tertiary,
        Color(0xFF7C4DFF),
        Color(0xFF26C6DA),
        Color(0xFFFFCA28),
        Color(0xFF66BB6A),
        Color(0xFFEF5350),
        Color(0xFFAB47BC),
    )
}
