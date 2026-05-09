package com.amdevstudio.budgetsense.ui.components

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Same ordering as Home "Top expenses" tiles (index 0 = largest category in [slices]).
 * Extra entries cover 6th/7th categories and "Other" in the ring chart.
 */
fun expenseCategoryDashboardPalette(): List<Color> = listOf(
    Color(0xFFFF8A50),
    Color(0xFF2DD4BF),
    Color(0xFF60A5FA),
    Color(0xFFA78BFA),
    Color(0xFFFBBF24),
    Color(0xFFFB7185),
    Color(0xFF34D399),
    Color(0xFF94A3B8),
)

private fun lerpFloat(start: Float, end: Float, t: Float): Float = start + (end - start) * t

@Composable
fun MonthExpensePieChart(
    currencyCode: String,
    hideMoney: Boolean,
    slices: List<Pair<String, Long>>,
    modifier: Modifier = Modifier,
    sliceColors: List<Color> = expenseCategoryDashboardPalette(),
) {
    val totalCents = slices.sumOf { it.second }.coerceAtLeast(1L)
    val total = totalCents.toFloat()
    val palette = sliceColors
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
            // Keep the original ring thickness, but still fit labels inside it.
            val stroke = min(size.width, size.height) * 0.11f
            val diameter = min(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            val ring = Stroke(width = stroke, cap = StrokeCap.Round)
            var start = -90f
            val cx = topLeft.x + diameter / 2f
            val cy = topLeft.y + diameter / 2f
            val rLabel = diameter / 2f - stroke / 2f

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

            val pctPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                textAlign = AndroidPaint.Align.CENTER
                textSize = 12.sp.toPx()
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(4.dp.toPx(), 0f, 1.dp.toPx(), 0xC8000000.toInt())
            }

            data class Label(
                val midDeg: Float,
                val text: String,
                val sliceColor: Color,
                val radius: Float,
                val textSizePx: Float,
            )

            val labels = mutableListOf<Label>()

            val paletteSize = palette.size.coerceAtLeast(1)
            slices.forEachIndexed { index, (_, cents) ->
                val sweepFull = (cents / total) * 360f
                val sweep = sweepFull * progress
                val pctFloat = (cents * 100f) / total
                val pctInt = pctFloat.roundToInt().coerceIn(0, 100)
                if (sweep > 0.05f) {
                    val sliceColor = palette[index % paletteSize]
                    drawArc(
                        color = sliceColor,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = ring,
                    )
                    // Label every category. For tiny slices, shrink text and stagger radius slightly
                    // to keep overlaps manageable.
                    val midDeg = start + sweep / 2f
                    val labelText = if (pctFloat in 0f..0.5f && cents > 0L) "<1%" else "$pctInt%"
                    val smallness = (sweepFull / 20f).coerceIn(0f, 1f) // 0=very small, 1=normal+
                    val textSizePx = lerpFloat(8.sp.toPx(), 11.sp.toPx(), smallness)
                    val radialNudge = if (index % 2 == 0) 1f else -1f
                    val radius = rLabel + radialNudge * (1f - smallness) * stroke * 0.28f
                    labels.add(
                        Label(
                            midDeg = midDeg,
                            text = labelText,
                            sliceColor = sliceColor,
                            radius = radius,
                            textSizePx = textSizePx,
                        ),
                    )
                    start += sweep
                }
            }

            labels.forEach { label ->
                val luminance =
                    label.sliceColor.red * 0.299f +
                        label.sliceColor.green * 0.587f +
                        label.sliceColor.blue * 0.114f
                pctPaint.color =
                    if (luminance > 0.62f) 0xFF1A2744.toInt() else android.graphics.Color.WHITE
                pctPaint.textSize = label.textSizePx
                val rad = Math.toRadians(label.midDeg.toDouble())
                val x = cx + label.radius * cos(rad).toFloat()
                val y = cy + label.radius * sin(rad).toFloat()
                val fm = pctPaint.fontMetrics
                val textY = y - (fm.ascent + fm.descent) / 2f
                drawContext.canvas.nativeCanvas.drawText(label.text, x, textY, pctPaint)
            }
        }

        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val legendPaletteSize = palette.size.coerceAtLeast(1)
            slices.forEachIndexed { index, (name, _) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Canvas(Modifier.size(10.dp)) {
                        drawCircle(color = palette[index % legendPaletteSize])
                    }
                    Text(
                        name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
