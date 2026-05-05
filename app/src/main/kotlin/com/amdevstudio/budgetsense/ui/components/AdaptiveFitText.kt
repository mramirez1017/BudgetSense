package com.amdevstudio.budgetsense.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/** Scales [TextStyle.fontSize] when it is in `sp` units. */
internal fun TextStyle.scaleSp(multiplier: Float): TextStyle {
    if (!fontSize.isSpecified || fontSize.type != TextUnitType.Sp) return this
    val v = (fontSize.value * multiplier).coerceAtLeast(6.5f)
    return copy(fontSize = v.sp)
}

/** Longer strings get a smaller base scale; narrow [BoxWithConstraints] widths shrink further. */
private fun layoutScaleForValue(charCount: Int, maxWidthPx: Int, density: Density): Float {
    val lengthScale = when {
        charCount >= 20 -> 0.58f
        charCount >= 16 -> 0.66f
        charCount >= 13 -> 0.74f
        charCount >= 11 -> 0.82f
        charCount >= 9 -> 0.9f
        else -> 1f
    }
    val widthScale = when {
        maxWidthPx <= 0 || maxWidthPx == Constraints.Infinity -> 1f
        else -> {
            val referencePx = with(density) { 128.dp.roundToPx() }.coerceAtLeast(1)
            (maxWidthPx.toFloat() / referencePx.toFloat()).coerceIn(0.42f, 1f)
        }
    }
    return (lengthScale * widthScale).coerceIn(0.42f, 1f)
}

/**
 * Monospace numbers (amounts) that shrink when the string is long or the available width is tight.
 * Place inside a width-bounded parent (e.g. [BoxWithConstraints] with [Modifier.fillMaxWidth]) for best results.
 */
@Composable
fun AdaptiveMonospaceValue(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    compact: Boolean,
    textAlign: TextAlign = TextAlign.End,
    minScale: Float = 0.45f,
) {
    val base = if (compact) {
        MaterialTheme.typography.titleSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp,
        )
    } else {
        MaterialTheme.typography.titleLarge.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp,
        )
    }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (textAlign == TextAlign.End) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        val scale = remember(text, constraints.maxWidth, density) {
            layoutScaleForValue(text.length, constraints.maxWidth, density).coerceAtLeast(minScale)
        }
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            style = base.scaleSp(scale),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
        )
    }
}

/** Non-monospace body/label text that scales down when long or width is tight. */
@Composable
fun AdaptivePlainText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    style: TextStyle,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 2,
    minScale: Float = 0.55f,
) {
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = when (textAlign) {
            TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
            TextAlign.Center -> Alignment.Center
            else -> Alignment.CenterStart
        },
    ) {
        val scale = remember(text, constraints.maxWidth, density) {
            layoutScaleForValue(text.length, constraints.maxWidth, density).coerceAtLeast(minScale)
        }
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            style = style.scaleSp(scale),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
        )
    }
}
