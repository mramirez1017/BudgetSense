package com.amdevstudio.budgetsense.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun rememberKeyboardDismiss(): () -> Unit {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    return remember(keyboard, focusManager) {
        {
            keyboard?.hide()
            focusManager.clearFocus(force = true)
        }
    }
}
