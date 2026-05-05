package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.remote.FrankfurterLatestResult
import com.amdevstudio.budgetsense.data.remote.FrankfurterRatesClient
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.domain.SupportedCurrencies
import com.amdevstudio.budgetsense.domain.currencyChipLabel
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
import com.amdevstudio.budgetsense.ui.util.rememberNetworkAvailable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

private data class ConvertOutcome(
    val result: FrankfurterLatestResult?,
    val message: String?,
)

private suspend fun runCurrencyFetch(
    amountText: String,
    fromCurrency: String,
    toCurrency: String,
    networkAvailable: Boolean,
): ConvertOutcome {
    if (!networkAvailable) {
        return ConvertOutcome(null, "You're offline — connect to the Internet to fetch live rates.")
    }
    val amt = amountText.trim().replace(",", ".").toDoubleOrNull()
    if (amt == null || amt <= 0.0) {
        return ConvertOutcome(null, "Enter a positive number to convert.")
    }
    val res = FrankfurterRatesClient.fetchConversion(amt, fromCurrency, toCurrency)
    return res.fold(
        onSuccess = { ConvertOutcome(it, null) },
        onFailure = { e -> ConvertOutcome(null, e.message ?: "Could not fetch rates.") },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    profileCurrency: String,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val networkAvailable by rememberNetworkAvailable()

    var amountText by remember { mutableStateOf("1") }
    var from by remember(profileCurrency) { mutableStateOf(profileCurrency.uppercase()) }
    var to by remember(profileCurrency) {
        val defaultOther = SupportedCurrencies.firstOrNull { it != profileCurrency.uppercase() } ?: "USD"
        mutableStateOf(defaultOther)
    }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<FrankfurterLatestResult?>(null) }

    LaunchedEffect(from, to, networkAvailable, amountText) {
        delay(420)
        if (!isActive) return@LaunchedEffect
        loading = true
        error = null
        val outcome = runCurrencyFetch(amountText, from, to, networkAvailable)
        loading = false
        if (!isActive) return@LaunchedEffect
        result = outcome.result
        error = outcome.message
    }

    Box(Modifier.fillMaxSize()) {
        BudgetSenseAmbientBackground(Modifier.fillMaxSize())
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                TopAppBar(
                    title = { Text("Currency converter") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Row {
                            ScreenHelpIconButton(title = "Currency converter") {
                                Text(
                                    "Rates are reference ECB-based data (via Frankfurter) for planning only — not bank or card settlement prices. Weekend and holiday updates may lag to the last published day.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "Enter an amount, choose From and To, and the result updates shortly after you edit. Use Refresh for a new fetch. You need Internet access.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        loading = true
                                        error = null
                                        val outcome = runCurrencyFetch(amountText, from, to, networkAvailable)
                                        loading = false
                                        result = outcome.result
                                        error = outcome.message
                                    }
                                },
                                enabled = !loading && networkAvailable,
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh rates")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OverlineCaps("Rates", color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${from.uppercase()})") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )

                NeoPanel(borderAlpha = 0.28f) {
                    Text("From", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
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
                                selected = from == code,
                                onClick = { from = code },
                                label = { Text(currencyChipLabel(code)) },
                                colors = chipColors,
                            )
                        }
                    }
                }

                NeoPanel(borderAlpha = 0.28f) {
                    Text("To", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
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
                                selected = to == code,
                                onClick = { to = code },
                                label = { Text(currencyChipLabel(code)) },
                                colors = chipColors,
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            val outcome = runCurrencyFetch(amountText, from, to, networkAvailable)
                            loading = false
                            result = outcome.result
                            error = outcome.message
                        }
                    },
                    enabled = !loading && networkAvailable,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Refresh now")
                }

                error?.let { msg ->
                    Text(msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }

                val r = result
                val busy = loading && r == null && error == null
                when {
                    busy -> RowCenteredSpinner()
                    r != null && from.uppercase() != to.uppercase() -> {
                        val cents = (r.convertedMajor * 100).roundToLong()
                        NeoPanel(borderAlpha = 0.32f) {
                            Text("Converted", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                MoneyFormat.format(to.uppercase(), cents, hide = false),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (r.rateDateIso.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "ECB publish date shown by provider: ${r.rateDateIso}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    from.uppercase() == to.uppercase() && amountText.trim().replace(",", ".").toDoubleOrNull()?.let { it > 0 } == true ->
                        Text("Same currency — no conversion needed.", style = MaterialTheme.typography.bodyMedium)
                }

                if (loading && r != null && from.uppercase() != to.uppercase()) {
                    RowCenteredSpinner()
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RowCenteredSpinner() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
