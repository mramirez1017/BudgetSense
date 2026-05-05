package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.util.rememberKeyboardDismiss
import java.math.BigDecimal
import java.math.RoundingMode

private val currencies = listOf("PHP", "USD", "EUR", "GBP", "JPY", "SGD", "AUD")

@Composable
fun ProfileSetupScreen(
    initial: UserProfileEntity,
    onSave: (UserProfileEntity) -> Unit,
) {
    val dismissKeyboard = rememberKeyboardDismiss()
    var name by remember { mutableStateOf(initial.displayName) }
    var currency by remember { mutableStateOf(initial.currencyCode) }
    var incomeText by remember {
        val major = if (initial.monthlyIncomeCents == 0L) {
            ""
        } else {
            BigDecimal(initial.monthlyIncomeCents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString()
        }
        mutableStateOf(major)
    }

    Box(Modifier.fillMaxSize()) {
        BudgetSenseAmbientBackground(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        OverlineCaps("Before you start", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(6.dp))
        Text("Set up your profile", style = MaterialTheme.typography.headlineSmall)
        Text(
            "We use this to format money and to suggest a default monthly budget. You can change everything later under Account.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display name") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            "Currency for all amounts",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(currencies, key = { it }) { code ->
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilterChip(
                    selected = currency == code,
                    onClick = { currency = code },
                    label = { Text(code) },
                    colors = chipColors,
                )
            }
        }

        OutlinedTextField(
            value = incomeText,
            onValueChange = { incomeText = it },
            label = { Text("Typical monthly income (optional)") },
            supportingText = {
                Text(
                    "Helps pre-fill budget ideas. Leave blank if you prefer to set budgets manually.",
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))
        Button(
            shape = MaterialTheme.shapes.medium,
            onClick = {
                dismissKeyboard()
                val cents = incomeText.trim().replace(",", ".").toBigDecimalOrNull()
                    ?.multiply(BigDecimal(100))?.toLong() ?: 0L
                onSave(
                    initial.copy(
                        displayName = name.trim().ifBlank { initial.displayName },
                        currencyCode = currency,
                        monthlyIncomeCents = cents,
                        onboardingComplete = true,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save and continue")
        }
        Spacer(Modifier.height(32.dp))
        }
    }
}
