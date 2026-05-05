package com.amdevstudio.budgetsense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BudgetSenseLightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = OnTealLight,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF134E4A),
    secondary = OrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = TealPrimaryDark,
    onTertiary = OnTealLight,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = Color(0xFFE2E8F0),
    error = Danger,
    onError = Color.White,
)

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
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) BudgetSenseDarkColorScheme else BudgetSenseLightColorScheme,
        typography = BudgetSenseTypography,
        shapes = BudgetSenseShapes,
        content = content,
    )
}
