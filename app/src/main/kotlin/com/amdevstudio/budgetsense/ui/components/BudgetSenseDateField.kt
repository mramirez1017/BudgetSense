package com.amdevstudio.budgetsense.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.amdevstudio.budgetsense.domain.Time
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSenseDateField(
    label: String,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val fmt = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val zone = ZoneId.systemDefault()
    val millis = remember(selectedDate) { selectedDate.atStartOfDay(zone).toInstant().toEpochMilli() }

    OutlinedTextField(
        value = selectedDate.format(fmt),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = { open = true },
        ),
        trailingIcon = {
            IconButton(onClick = { open = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Choose date")
            }
        },
    )

    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ms = state.selectedDateMillis ?: millis
                        onDateSelected(Instant.ofEpochMilli(ms).atZone(zone).toLocalDate())
                        open = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSenseOptionalDateField(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    placeholderWhenEmpty: String = "No deadline",
) {
    var open by remember { mutableStateOf(false) }
    val fmt = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val zone = ZoneId.systemDefault()
    val display = selectedDate?.format(fmt) ?: placeholderWhenEmpty
    val initialMillis = remember(selectedDate, open) {
        (selectedDate ?: LocalDate.now()).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = { open = true },
            ),
        trailingIcon = {
            Row {
                if (selectedDate != null) {
                    IconButton(onClick = { onDateSelected(null) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = { open = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choose date")
                }
            }
        },
    )

    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = {
                            onDateSelected(null)
                            open = false
                        },
                    ) { Text("Clear") }
                    TextButton(
                        onClick = {
                            val ms = state.selectedDateMillis ?: initialMillis
                            onDateSelected(Instant.ofEpochMilli(ms).atZone(zone).toLocalDate())
                            open = false
                        },
                    ) { Text("OK") }
                }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSenseMonthField(
    label: String,
    monthKey: String,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val display = remember(monthKey) { Time.formatMonthKey(monthKey) }

    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = { open = true },
        ),
        trailingIcon = {
            IconButton(onClick = { open = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Choose month")
            }
        },
    )

    if (open) {
        MonthYearPickerDialog(
            initialMonthKey = monthKey,
            onDismiss = { open = false },
            onConfirm = { picked ->
                onMonthSelected(picked)
                open = false
            },
        )
    }
}

@Composable
private fun MonthYearPickerDialog(
    initialMonthKey: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val current = remember { YearMonth.now() }
    val initial = remember(initialMonthKey) {
        val parts = initialMonthKey.split("-")
        YearMonth.of(parts[0].toInt(), parts[1].toInt())
    }

    var year by remember { mutableStateOf(initial.year) }
    var month by remember { mutableStateOf(initial.monthValue) } // 1..12

    val years = remember(current) { (2000..current.year).toList() }
    val monthNames = remember {
        listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )
    }

    // Prevent future months.
    val maxMonthForYear = if (year == current.year) current.monthValue else 12
    if (month > maxMonthForYear) month = maxMonthForYear

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select month") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Month selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { month = (month - 1).coerceAtLeast(1) }) { Text("◀") }
                    Text(
                        text = monthNames[month - 1],
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.alignByBaseline(),
                    )
                    TextButton(onClick = { month = (month + 1).coerceAtMost(maxMonthForYear) }) { Text("▶") }
                }

                // Year selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            val idx = years.indexOf(year)
                            if (idx > 0) year = years[idx - 1]
                        },
                    ) { Text("◀") }
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.alignByBaseline(),
                    )
                    TextButton(
                        onClick = {
                            val idx = years.indexOf(year)
                            if (idx in 0 until years.lastIndex) year = years[idx + 1]
                        },
                    ) { Text("▶") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val picked = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    onConfirm(picked)
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
