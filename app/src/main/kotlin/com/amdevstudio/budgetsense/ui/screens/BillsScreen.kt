package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.data.local.entity.BillReminderEntity
import com.amdevstudio.budgetsense.data.repository.BillRepository
import com.amdevstudio.budgetsense.domain.Time
import com.amdevstudio.budgetsense.ui.components.BudgetSenseDateField
import com.amdevstudio.budgetsense.ui.components.GlassCard
import com.amdevstudio.budgetsense.ui.util.appListContentPadding
import com.amdevstudio.budgetsense.ui.util.fabMaxDragDownPx
import com.amdevstudio.budgetsense.ui.util.rememberKeyboardDismiss
import com.amdevstudio.budgetsense.notifications.BillReminderScheduler
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    repository: BillRepository,
    bills: List<BillReminderEntity>,
    userId: String,
    onBack: () -> Unit,
    showTopBar: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val dismissKeyboard = rememberKeyboardDismiss()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var repeat by remember { mutableStateOf(true) }
    var notify by remember { mutableStateOf("1") }
    var billPendingDelete by remember { mutableStateOf<BillReminderEntity?>(null) }

    val density = LocalDensity.current
    var fabDragX by remember { mutableFloatStateOf(0f) }
    var fabDragY by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()
        val fabPx = with(density) { 56.dp.roundToPx() }.toFloat()
        val padH = with(density) { 16.dp.toPx() }
        val padV = with(density) { 24.dp.toPx() }
        val maxDragLeft = -(maxW - fabPx - padH * 2).coerceAtLeast(0f)
        val maxDragUp = -(maxH - fabPx - padV * 2).coerceAtLeast(0f)
        val maxDragDown = fabMaxDragDownPx(density)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = if (!showTopBar) {
                {}
            } else {
                {
                    TopAppBar(
                        title = { Text("Bill reminders") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 24.dp)
                        .offset { IntOffset(fabDragX.roundToInt(), fabDragY.roundToInt()) }
                        .pointerInput(maxDragLeft, maxDragUp, maxDragDown, fabPx) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                fabDragX = (fabDragX + dragAmount.x).coerceIn(maxDragLeft, 0f)
                                fabDragY = (fabDragY + dragAmount.y).coerceIn(maxDragUp, maxDragDown)
                            }
                        },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add bill — drag to move")
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!showTopBar) {
                    Text("Bill reminders", style = MaterialTheme.typography.headlineSmall)
                }

                LazyColumn(
                    contentPadding = appListContentPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(bills, key = { it.id }) { bill ->
                        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 26.dp) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        bill.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(onClick = { billPendingDelete = bill }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete bill",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Text(
                                    "Due ${formatMillis(bill.dueAtMillis)} · remind ${bill.notifyDaysBefore} day(s) before",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = bill.lastPaidPeriod == Time.monthKey(),
                                        onCheckedChange = { checked ->
                                            scope.launch {
                                                repository.upsert(
                                                    userId,
                                                    bill.copy(
                                                        lastPaidPeriod = if (checked) Time.monthKey() else null,
                                                    ),
                                                )
                                                BillReminderScheduler.schedule(
                                                    context = context,
                                                    billId = bill.id,
                                                    title = bill.title,
                                                    dueAtMillis = bill.dueAtMillis,
                                                    notifyDaysBefore = bill.notifyDaysBefore,
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
                            userId = userId,
                            title = title.trim().ifBlank { "Bill" },
                            dueAtMillis = millis,
                            repeatMonthly = repeat,
                            notifyDaysBefore = notify.toIntOrNull()?.coerceAtLeast(0) ?: 1,
                            lastPaidPeriod = null,
                        )
                        // Close the dialog immediately so a cloud rule/network failure doesn't trap the user.
                        showDialog = false
                        title = ""
                        dueDate = LocalDate.now()
                        repeat = true
                        notify = "1"
                        scope.launch {
                            runCatching {
                                repository.upsert(userId, entity)
                            }.onSuccess {
                                BillReminderScheduler.schedule(
                                    context = context,
                                    billId = entity.id,
                                    title = entity.title,
                                    dueAtMillis = entity.dueAtMillis,
                                    notifyDaysBefore = entity.notifyDaysBefore,
                                )
                            }
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        )
    }

    billPendingDelete?.let { bill ->
        AlertDialog(
            onDismissRequest = { billPendingDelete = null },
            title = { Text("Delete bill?") },
            text = {
                Text(
                    "“${bill.title}” will be removed and its reminder will be cancelled on this phone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        billPendingDelete = null
                        scope.launch {
                            runCatching {
                                repository.delete(userId, bill)
                            }
                            BillReminderScheduler.cancel(context, bill.id)
                        }
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { billPendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

private fun formatMillis(millis: Long): String {
    val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return DateTimeFormatter.ISO_LOCAL_DATE.format(d)
}

