package com.amdevstudio.budgetsense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BudgetSenseLightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = OnPurple,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = BlueAction,
    onSecondary = OnBlueAction,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = PurplePrimaryDark,
    onTertiary = OnPurple,
    background = LilacBackground,
    onBackground = TextStrong,
    surface = CardSurface,
    onSurface = TextNormal,
    surfaceVariant = CardSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = OutlineSoft,
    outlineVariant = OutlineHairline,
    error = Danger,
    onError = Color.White,
)

private val BudgetSenseDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = OnDark,
    primaryContainer = NeonSurface2,
    onPrimaryContainer = NeonText,
    secondary = NeonTeal,
    onSecondary = OnDark,
    secondaryContainer = NeonSurface2,
    onSecondaryContainer = NeonText,
    tertiary = NeonPurple,
    onTertiary = OnDark,
    background = NeonBg,
    onBackground = NeonText,
    surface = NeonSurface,
    onSurface = NeonText,
    surfaceVariant = NeonSurface2,
    onSurfaceVariant = NeonTextMuted,
    outline = NeonOutline,
    outlineVariant = Color(0xFF2A2A2A),
    error = Danger,
    onError = OnDark,
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
