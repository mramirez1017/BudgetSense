package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.data.repository.AuthRepository
import com.amdevstudio.budgetsense.data.repository.ProfileRepository
import com.amdevstudio.budgetsense.data.repository.TransactionRepository
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    profile: UserProfileEntity,
    profileRepository: ProfileRepository,
    authRepository: AuthRepository,
    transactionRepository: TransactionRepository,
    onSignedOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OverlineCaps("Account", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text("Preferences & sign-in", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Hide amounts on Home and Money if you’re showing the screen to someone else.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        NeoPanel(borderAlpha = 0.28f) {
            Text("Privacy on this device", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Hide balances on dashboards",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Switch(
                checked = profile.hideBalance,
                onCheckedChange = { checked ->
                    val updated = profile.copy(hideBalance = checked)
                    scope.launch {
                        profileRepository.save(updated)
                        withContext(Dispatchers.IO) {
                            profileRepository.syncProfileToCloud(updated)
                        }
                    }
                },
            )
        }

        Text(
            "Sign out removes your session on this phone and deletes BudgetSense data stored locally on the device. Sign in again anytime.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = {
                scope.launch {
                    transactionRepository.clearAllLocal(profile.userId)
                    authRepository.signOut(context)
                    onSignedOut()
                }
            },
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Sign out") }
    }
}
