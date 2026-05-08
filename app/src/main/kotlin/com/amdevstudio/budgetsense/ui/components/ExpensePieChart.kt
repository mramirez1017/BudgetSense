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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.domain.MoneyFormat
import kotlin.math.min

/**
 * Animated ring chart for expense breakdown. [slices] are category name to cents.
 */
@Composable
fun MonthExpensePieChart(
    currencyCode: String,
    hideMoney: Boolean,
    slices: List<Pair<String, Long>>,
    modifier: Modifier = Modifier,
) {
    val totalCents = slices.sumOf { it.second }.coerceAtLeast(1L)
    val total = totalCents.toFloat()
    val palette = chartPalette()
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
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
            val stroke = min(size.width, size.height) * 0.11f
            val diameter = min(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            val ring = Stroke(width = stroke, cap = StrokeCap.Round)
            var start = -90f

            // Track ring behind slices
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = ring,
            )

            slices.forEachIndexed { index, (_, cents) ->
                val sweep = (cents / total) * 360f * progress
                if (sweep > 0.05f) {
                    drawArc(
                        color = palette[index % palette.size],
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = ring,
                    )
                    start += sweep
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Total: ${MoneyFormat.format(currencyCode, totalCents, hideMoney)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
