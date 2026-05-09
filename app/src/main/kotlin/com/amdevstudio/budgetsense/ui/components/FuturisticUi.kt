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
import com.amdevstudio.budgetsense.ui.theme.HeaderGradientEnd
import com.amdevstudio.budgetsense.ui.theme.HeaderGradientMid
import com.amdevstudio.budgetsense.ui.theme.HeaderGradientStart

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
    Canvas(modifier.fillMaxSize()) {
        drawRect(color = base)
        val w = size.width
        val h = size.height
        // Top purple header glow
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(HeaderGradientMid.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(w * 0.55f, -h * 0.05f),
                radius = w.coerceAtLeast(h) * 0.90f,
            ),
        )
        // Left blob
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(HeaderGradientStart.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(-w * 0.08f, h * 0.12f),
                radius = w.coerceAtLeast(h) * 0.72f,
            ),
        )
        // Right blob
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(HeaderGradientEnd.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(w * 1.05f, h * 0.30f),
                radius = w.coerceAtLeast(h) * 0.78f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.25f, h * 1.06f),
                radius = w.coerceAtLeast(h) * 0.70f,
            ),
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, base.copy(alpha = 0.92f)),
                startY = h * 0.30f,
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
