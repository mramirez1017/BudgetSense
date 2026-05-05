package com.amdevstudio.budgetsense.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.repository.AuthRepository
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
import com.amdevstudio.budgetsense.ui.util.findActivity
import com.amdevstudio.budgetsense.ui.util.userFacingMessage
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onSignedIn: () -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        scope.launch {
            val r = authRepository.finishGoogleSignIn(result.data)
            r.onSuccess { onSignedIn() }
                .onFailure { onError(it.userFacingMessage(fallback = "Sign-in failed")) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        BudgetSenseAmbientBackground(Modifier.fillMaxSize())
        ScreenHelpIconButton(
            title = "BudgetSense sign-in",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp),
        ) {
            Text(
                "Track what you earn and spend, set a monthly budget, and get simple tips from your own data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Works on your phone offline. Sign in with Google to keep your profile and activity available when you switch devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OverlineCaps("Personal finance", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text("BudgetSense", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(36.dp))
            Button(
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val activity = context.findActivity()
                    if (activity == null) {
                        onError("No activity context")
                        return@Button
                    }
                    launcher.launch(authRepository.googleSignInIntent(activity))
                },
            ) {
                Text("Continue with Google")
            }
        }
    }
}
