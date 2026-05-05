package com.amdevstudio.budgetsense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverlineCaps(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 2.2.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        color = color,
    )
}

fun Modifier.futuristicFrame(
    shape: Shape,
    accent: Color,
    alpha: Float = 0.35f,
): Modifier = border(1.dp, accent.copy(alpha = alpha), shape)

/** Soft mesh lighting behind screens — reads more bespoke than flat Material background. */
@Composable
fun BudgetSenseAmbientBackground(modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.background
    val warm = MaterialTheme.colorScheme.primary
    Canvas(modifier.fillMaxSize()) {
        drawRect(color = base)
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(warm.copy(alpha = 0.09f), Color.Transparent),
                center = Offset(w * 0.9f, -h * 0.08f),
                radius = w.coerceAtLeast(h) * 0.72f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF5C3DFF).copy(alpha = 0.05f), Color.Transparent),
                center = Offset(-w * 0.02f, h * 1.02f),
                radius = w.coerceAtLeast(h) * 0.65f,
            ),
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, base.copy(alpha = 0.85f)),
                startY = h * 0.42f,
                endY = h,
            ),
        )
    }
}

/** Frosted-style panel: translucent fill + accent rim (less “stock Card” than M3 defaults). */
@Composable
fun NeoPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    borderAlpha: Float = 0.22f,
    fillAlpha: Float = 1f,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = fillAlpha))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha), shape)
            .padding(20.dp),
        content = content,
    )
}

@Composable
fun DataFigure(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    compact: Boolean = false,
) {
    AdaptiveMonospaceValue(
        text = text,
        modifier = modifier,
        color = color,
        compact = compact,
        textAlign = TextAlign.End,
    )
}
