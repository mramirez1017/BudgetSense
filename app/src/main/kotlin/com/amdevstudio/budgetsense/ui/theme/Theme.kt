package com.amdevstudio.budgetsense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BudgetSenseDarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = OnAccentDark,
    primaryContainer = Color(0xFF3D2415),
    onPrimaryContainer = Color(0xFFFFD4BF),
    secondary = SurfaceElevated,
    onSecondary = TextPrimaryLight,
    secondaryContainer = SurfaceCharcoal,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = AccentOrangeDim,
    onTertiary = OnAccentDark,
    background = BlackPure,
    onBackground = TextPrimaryLight,
    surface = SurfaceCharcoal,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = OutlineSubtle,
    outlineVariant = Color(0xFF2A2A2A),
    error = Danger,
    onError = BlackPure,
)

@Composable
fun BudgetSenseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BudgetSenseDarkColorScheme,
        typography = BudgetSenseTypography,
        shapes = BudgetSenseShapes,
        content = content,
    )
}
