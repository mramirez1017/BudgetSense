package com.amdevstudio.budgetsense.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.repository.AuthRepository
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.GlassCard
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
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
    val lockscreenDrawableId = remember(context) {
        context.resources.getIdentifier("lockscreen", "drawable", context.packageName)
    }
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
        if (lockscreenDrawableId != 0) {
            Image(
                painter = painterResource(lockscreenDrawableId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
            // Light scrim to keep text readable while preserving the artwork.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.78f)),
            )
        } else {
            BudgetSenseAmbientBackground(Modifier.fillMaxSize())
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 30.dp,
                accent = MaterialTheme.colorScheme.primary,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OverlineCaps("Personal finance", color = MaterialTheme.colorScheme.primary)
                    Text("BudgetSense", style = MaterialTheme.typography.displaySmall)
                    Text(
                        "Guiding your finances effortlessly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
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
                        Text("Continue with Google", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
