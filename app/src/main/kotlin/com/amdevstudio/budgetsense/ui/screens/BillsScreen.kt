package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.entity.BillReminderEntity
import com.amdevstudio.budgetsense.data.repository.BillRepository
import com.amdevstudio.budgetsense.domain.Time
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
import com.amdevstudio.budgetsense.ui.components.BudgetSenseDateField
import com.amdevstudio.budgetsense.ui.util.rememberKeyboardDismiss
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    repository: BillRepository,
    bills: List<BillReminderEntity>,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dismissKeyboard = rememberKeyboardDismiss()
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var repeat by remember { mutableStateOf(true) }
    var notify by remember { mutableStateOf("1") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ScreenHelpIconButton(title = "Bill reminders") {
                        Text(
                            "Add a bill with a name, due date, and whether it repeats each month. Choose how many days before the due date you want a heads-up. Check “Paid this month” after you pay so you can track it against the current month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add bill")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(bills, key = { it.id }) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(bill.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Due ${formatMillis(bill.dueAtMillis)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = bill.lastPaidPeriod == Time.monthKey(),
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        repository.upsert(
                                            bill.copy(
                                                lastPaidPeriod = if (checked) Time.monthKey() else null,
                                            ),
                                        )
                                    }
                                },
                            )
                            Text("Paid this month")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New bill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Name") })
                    BudgetSenseDateField(
                        label = "Due date",
                        selectedDate = dueDate,
                        onDateSelected = { dueDate = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = repeat, onCheckedChange = { repeat = it })
                        Text("Repeat monthly")
                    }
                    OutlinedTextField(
                        value = notify,
                        onValueChange = { notify = it },
                        label = { Text("Remind days before") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        dismissKeyboard()
                        val entity = BillReminderEntity(
                            id = UUID.randomUUID().toString(),
                            title = title.trim().ifBlank { "Bill" },
                            dueAtMillis = millis,
                            repeatMonthly = repeat,
                            notifyDaysBefore = notify.toIntOrNull()?.coerceAtLeast(0) ?: 1,
                            lastPaidPeriod = null,
                        )
                        scope.launch {
                            try {
                                repository.upsert(entity)
                            } catch (_: Exception) {
                                return@launch
                            }
                            showDialog = false
                            title = ""
                            dueDate = LocalDate.now()
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        )
    }
}

private fun formatMillis(millis: Long): String {
    val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return DateTimeFormatter.ISO_LOCAL_DATE.format(d)
}

