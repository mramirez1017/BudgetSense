package com.amdevstudio.budgetsense.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class PillNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun PillBottomNav(
    items: List<PillNavItem>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    val scheme = MaterialTheme.colorScheme
    val infinite = rememberInfiniteTransition(label = "pillNavGradient")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shift",
    )
    val g0 = scheme.primary.copy(alpha = 0.34f)
    val g1 = scheme.tertiary.copy(alpha = 0.28f)
    val g2 = scheme.secondary.copy(alpha = 0.30f)
    val g3 = scheme.primaryContainer.copy(alpha = 0.55f)
    val g4 = scheme.surface.copy(alpha = 0.88f)
    val g5 = scheme.secondaryContainer.copy(alpha = 0.40f)

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.10f),
            )
            .clip(shape)
            .drawBehind {
                val w = size.width
                val h = size.height
                val travel = (w + h) * 1.35f
                val p = shift * travel
                val brush = Brush.linearGradient(
                    colors = listOf(g0, g1, g2, g3, g4, g5, g0),
                    start = Offset(p - travel * 0.4f, h * 0.15f),
                    end = Offset(p + travel * 0.55f, h * 0.95f),
                )
                val r = minOf(w, h) / 2f
                drawRoundRect(brush = brush, cornerRadius = CornerRadius(r, r))
            },
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.id == selectedId
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = if (selected) 18.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = if (selected) 0.14f else 0f),
                            spotColor = Color.Black.copy(alpha = if (selected) 0.14f else 0f),
                        )
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                        )
                        .clickable { onSelected(item.id) }
                        .padding(if (selected) 12.dp else 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(if (selected) 26.dp else 22.dp),
                    )
                }
            }
        }
    }
}
