package com.amdevstudio.budgetsense.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reserve space for the floating pill bottom bar + gesture navigation area.
 * Content still draws behind the bar; this padding keeps the last rows readable above it.
 */
private val PillBottomBarReserve = 96.dp

/** Lets draggable FABs move slightly below their default anchor (toward the bottom / over the floating pill). */
fun fabMaxDragDownPx(density: Density): Float = with(density) { 112.dp.toPx() }

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

