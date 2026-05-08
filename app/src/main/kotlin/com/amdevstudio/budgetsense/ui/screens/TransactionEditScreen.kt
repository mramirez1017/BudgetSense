package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.repository.TransactionRepository
import com.amdevstudio.budgetsense.domain.Categories
import com.amdevstudio.budgetsense.ui.components.BudgetSenseDateField
import com.amdevstudio.budgetsense.ui.components.GlassCard
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
import com.amdevstudio.budgetsense.ui.util.rememberKeyboardDismiss
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(
    txId: String,
    userId: String?,
    repository: TransactionRepository,
    onDone: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dismissKeyboard = rememberKeyboardDismiss()
    var loaded by remember { mutableStateOf<TransactionEntity?>(null) }
    LaunchedEffect(txId, userId) {
        loaded = when {
            txId == "new" -> null
            userId == null -> null
            else -> repository.get(userId, txId)
        }
    }

    var type by remember(loaded) { mutableStateOf(loaded?.type ?: TransactionType.EXPENSE) }
    var category by remember(loaded) { mutableStateOf(loaded?.category ?: Categories.expense.first()) }
    var amount by remember(loaded) {
        mutableStateOf(
            loaded?.let {
                BigDecimal(it.amountCents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros()
                    .toPlainString()
            }.orEmpty().ifBlank { "" },
        )
    }
    var note by remember(loaded) { mutableStateOf(loaded?.note.orEmpty()) }
    var selectedDate by remember(loaded) {
        val millis = loaded?.occurredAtMillis ?: System.currentTimeMillis()
        mutableStateOf(
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate(),
        )
    }
    var typeMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }

    val categoryOptions = remember(type) { Categories.defaultsFor(type) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (txId == "new") "Add transaction" else "Edit transaction") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ScreenHelpIconButton(title = "Add or edit a transaction") {
                        Text(
                            "Choose income or expense, pick a category, and enter the amount (use decimals if your currency uses them). Notes are optional.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Set the date when the money actually moved. Save stores the entry; on an existing item you can Delete to remove it permanently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Your transaction will appear under the month you selected on Home, based on the date you choose here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            cornerRadius = 28.dp,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {

        ExposedDropdownMenuBox(expanded = typeMenu, onExpandedChange = { typeMenu = !typeMenu }) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = type.name,
                onValueChange = {},
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenu) },
            )
            DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                TransactionType.entries.forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t.name) },
                        onClick = {
                            type = t
                            category = Categories.defaultsFor(t).first()
                            typeMenu = false
                        },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(expanded = categoryMenu, onExpandedChange = { categoryMenu = !categoryMenu }) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = category,
                onValueChange = {},
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenu) },
            )
            DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                categoryOptions.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                            category = c
                            categoryMenu = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
        )
        BudgetSenseDateField(
            label = "Date",
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val cents = amount.trim().replace(",", ".").toBigDecimalOrNull()
                        ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()
                    if (cents == null || cents <= 0L) return@Button
                    val millis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    dismissKeyboard()
                    scope.launch {
                        try {
                            repository.upsert(
                                uid = userId,
                                id = if (txId == "new") null else txId,
                                type = type,
                                category = category,
                                amountCents = cents,
                                note = note.trim(),
                                occurredAtMillis = millis,
                            )
                        } catch (_: Exception) {
                            return@launch
                        }
                        onDone()
                    }
                },
                modifier = Modifier.weight(1f),
            ) { Text("Save") }

            if (txId != "new") {
                OutlinedButton(
                    onClick = {
                        val existing = loaded ?: return@OutlinedButton
                        dismissKeyboard()
                        scope.launch {
                            try {
                                repository.delete(userId, existing)
                            } catch (_: Exception) {
                                return@launch
                            }
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Delete") }
            }
        }
            }
        }
    }
}
