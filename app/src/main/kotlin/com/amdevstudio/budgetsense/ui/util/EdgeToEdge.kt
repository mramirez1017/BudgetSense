package com.amdevstudio.budgetsense.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reserve space for the custom pill bottom bar + gesture navigation area.
 * This keeps scroll content from ending up under the bottom bar on smaller devices.
 */
private val PillBottomBarReserve = 96.dp

@Composable
fun Modifier.appBottomBarSafePadding(): Modifier {
    // Compose versions vary on WindowInsets APIs; navigationBarsPadding() is stable.
    // We apply system nav-bar padding plus a fixed reserve for the custom pill bar.
    return this
        .navigationBarsPadding()
        .padding(bottom = PillBottomBarReserve)
}

@Composable
fun appListContentPadding(extraTop: Dp = 0.dp): PaddingValues {
    return PaddingValues(
        top = extraTop,
        bottom = PillBottomBarReserve,
    )
}

