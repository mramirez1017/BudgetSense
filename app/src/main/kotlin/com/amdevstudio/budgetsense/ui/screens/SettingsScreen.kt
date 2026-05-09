package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.data.repository.AuthRepository
import com.amdevstudio.budgetsense.data.repository.ProfileRepository
import com.amdevstudio.budgetsense.data.repository.TransactionRepository
import com.amdevstudio.budgetsense.domain.SupportedCurrencies
import com.amdevstudio.budgetsense.domain.currencyChipLabel
import com.amdevstudio.budgetsense.ui.components.GlassCard
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.util.appBottomBarSafePadding
import com.amdevstudio.budgetsense.ui.util.rememberKeyboardDismiss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    profile: UserProfileEntity,
    profileRepository: ProfileRepository,
    authRepository: AuthRepository,
    transactionRepository: TransactionRepository,
    appPrefs: android.content.SharedPreferences,
    onOpenAbout: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenCurrencyConverter: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dismissKeyboard = rememberKeyboardDismiss()
    val appLockKey = "app_lock_enabled"
    var appLockEnabled by remember { mutableStateOf(appPrefs.getBoolean(appLockKey, false)) }
    var displayNameDraft by remember(profile.userId) { mutableStateOf(profile.displayName) }
    LaunchedEffect(profile.displayName) {
        displayNameDraft = profile.displayName
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .appBottomBarSafePadding()
            .padding(20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            OverlineCaps("Account", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text("Preferences & sign-in", style = MaterialTheme.typography.headlineSmall)
        }

        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Your name", style = MaterialTheme.typography.titleMedium)
                Text(
                    "How BudgetSense greets you on Home.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = displayNameDraft,
                    onValueChange = { displayNameDraft = it },
                    label = { Text("Display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        val trimmed = displayNameDraft.trim().ifBlank { "You" }
                        dismissKeyboard()
                        if (trimmed == profile.displayName) return@Button
                        val updated = profile.copy(displayName = trimmed)
                        scope.launch {
                            profileRepository.save(updated)
                            withContext(Dispatchers.IO) {
                                profileRepository.syncProfileToCloud(updated)
                            }
                        }
                    },
                    enabled = displayNameDraft.trim().ifBlank { "You" } != profile.displayName,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Text("Save name")
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Help & info", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onOpenAbout, modifier = Modifier.fillMaxWidth()) {
                    Text("About BudgetSense")
                }
                TextButton(onClick = onOpenFaq, modifier = Modifier.fillMaxWidth()) {
                    Text("FAQ")
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Currency", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(SupportedCurrencies, key = { it }) { code ->
                        val chipColors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FilterChip(
                            selected = profile.currencyCode == code,
                            onClick = {
                                if (profile.currencyCode == code) return@FilterChip
                                val updated = profile.copy(currencyCode = code)
                                scope.launch {
                                    profileRepository.save(updated)
                                    withContext(Dispatchers.IO) {
                                        profileRepository.syncProfileToCloud(updated)
                                    }
                                }
                            },
                            label = { Text(currencyChipLabel(code)) },
                            colors = chipColors,
                        )
                    }
                }
                TextButton(onClick = onOpenCurrencyConverter, modifier = Modifier.fillMaxWidth()) {
                    Text("Open live currency converter")
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Privacy on this device", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Hide balances on dashboards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        }

        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("App lock", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Require Face/Fingerprint or device PIN/pattern/password to open BudgetSense.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Switch(
                    checked = appLockEnabled,
                    onCheckedChange = { checked ->
                        appLockEnabled = checked
                        appPrefs.edit().putBoolean(appLockKey, checked).apply()
                    },
                )
            }
        }

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
