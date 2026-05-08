package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.amdevstudio.budgetsense.data.repository.SavingsRepository
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.ui.components.BudgetSenseOptionalDateField
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton
import com.amdevstudio.budgetsense.ui.util.rememberKeyboardDismiss
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    profile: UserProfileEntity,
    userId: String,
    repository: SavingsRepository,
    goals: List<SavingsGoalEntity>,
    onBack: () -> Unit,
) {
    val contributions by repository.observeAllContributions(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val byGoal = contributions.groupBy { it.goalId }

    val scope = rememberCoroutineScope()
    val dismissKeyboard = rememberKeyboardDismiss()
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var deadlineDate by remember { mutableStateOf<LocalDate?>(null) }
    var expandedGoalId by remember { mutableStateOf<String?>(null) }
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var goalPendingDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val dateFmt = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    }
    val zone = ZoneId.systemDefault()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings goals", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ScreenHelpIconButton(title = "Savings") {
                        Text(
                            "Collapse a goal to see recent deposits. Expand it for full history, edit details, delete the goal, or add money toward the target.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add goal")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(goals, key = { it.id }) { goal ->
                val open = expandedGoalId == goal.id
                val sorted = byGoal[goal.id].orEmpty().sortedByDescending { it.createdAtMillis }
                val recent = sorted.take(3)
                val progress = if (goal.targetCents > 0) {
                    (goal.savedCents.toFloat() / goal.targetCents.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val overTarget = goal.targetCents > 0 && goal.savedCents >= goal.targetCents

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF26A69A).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = Color(0xFF26A69A),
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
                            ) {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "${sorted.size} deposit${if (sorted.size == 1) "" else "s"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { editingGoal = goal }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit goal")
                            }
                            IconButton(onClick = { goalPendingDelete = goal }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete goal")
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedGoalId = if (open) null else goal.id },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    MoneyFormat.format(profile.currencyCode, goal.savedCents, profile.hideBalance),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (overTarget) Color(0xFF43A047) else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "of ${MoneyFormat.format(profile.currencyCode, goal.targetCents, profile.hideBalance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (open) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = if (overTarget) Color(0xFF43A047) else Color(0xFF26A69A),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        )

                        if (!open) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Text(
                                    "Recent deposits",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (sorted.isEmpty()) {
                                    Text(
                                        "No deposits yet — use Add money below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    recent.forEach { c ->
                                        ContributionRow(
                                            contribution = c,
                                            currencyCode = profile.currencyCode,
                                            hide = profile.hideBalance,
                                            dateFmt = dateFmt,
                                            zone = zone,
                                            onRemove = null,
                                        )
                                    }
                                    if (sorted.size > recent.size) {
                                        Text(
                                            "Expand for full log (${sorted.size} deposits).",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = open,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            Column(Modifier.padding(top = 4.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Deposit history",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(4.dp))
                                if (sorted.isEmpty()) {
                                    Text(
                                        "No deposits yet — use Add money below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    sorted.forEach { c ->
                                        ContributionRow(
                                            contribution = c,
                                            currencyCode = profile.currencyCode,
                                            hide = profile.hideBalance,
                                            dateFmt = dateFmt,
                                            zone = zone,
                                            onRemove = {
                                                scope.launch {
                                                    try {
                                                        repository.removeContribution(userId, goal, c)
                                                    } catch (_: Exception) {
                                                        return@launch
                                                    }
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        GoalAddMoneyRow(
                            goal = goal,
                            profile = profile,
                            userId = userId,
                            repository = repository,
                            scope = scope,
                            dismissKeyboard = dismissKeyboard,
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New savings goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal name") })
                    OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target amount") })
                    BudgetSenseOptionalDateField(
                        label = "Deadline (optional)",
                        selectedDate = deadlineDate,
                        onDateSelected = { deadlineDate = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cents = target.replace(",", ".").toBigDecimalOrNull()
                            ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()
                            ?: return@TextButton
                        val deadlineMillis = deadlineDate?.let {
                            it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }
                        dismissKeyboard()
                        val entity = SavingsGoalEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            name = name.ifBlank { "Goal" },
                            targetCents = cents,
                            savedCents = 0L,
                            deadlineMillis = deadlineMillis,
                        )
                        scope.launch {
                            try {
                                repository.upsert(userId, entity)
                            } catch (_: Exception) {
                                return@launch
                            }
                            showDialog = false
                            name = ""
                            target = ""
                            deadlineDate = null
                        }
                    },
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        )
    }

    val editSeed = editingGoal
    if (editSeed != null) {
        val liveGoal = goals.firstOrNull { it.id == editSeed.id } ?: editSeed
        var editName by remember(liveGoal.id) { mutableStateOf(liveGoal.name) }
        var editTarget by remember(liveGoal.id) {
            mutableStateOf(
                BigDecimal(liveGoal.targetCents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString(),
            )
        }
        var editDeadline by remember(liveGoal.id) {
            mutableStateOf(
                liveGoal.deadlineMillis?.let {
                    Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
                },
            )
        }
        AlertDialog(
            onDismissRequest = { editingGoal = null },
            title = { Text("Edit savings goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Goal name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = editTarget,
                        onValueChange = { editTarget = it },
                        label = { Text("Target amount") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Saved so far: ${MoneyFormat.format(profile.currencyCode, liveGoal.savedCents, profile.hideBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BudgetSenseOptionalDateField(
                        label = "Deadline (optional)",
                        selectedDate = editDeadline,
                        onDateSelected = { editDeadline = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cents = editTarget.replace(",", ".").toBigDecimalOrNull()
                            ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()
                            ?: return@TextButton
                        if (cents < liveGoal.savedCents) return@TextButton
                        val deadlineMillis = editDeadline?.let {
                            it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }
                        dismissKeyboard()
                        scope.launch {
                            try {
                                repository.upsert(
                                    userId,
                                    liveGoal.copy(
                                        name = editName.ifBlank { liveGoal.name },
                                        targetCents = cents,
                                        deadlineMillis = deadlineMillis,
                                    ),
                                )
                            } catch (_: Exception) {
                                return@launch
                            }
                            editingGoal = null
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingGoal = null }) { Text("Cancel") } },
        )
    }

    goalPendingDelete?.let { g ->
        AlertDialog(
            onDismissRequest = { goalPendingDelete = null },
            title = { Text("Delete savings goal?") },
            text = {
                Text(
                    "“${g.name}” and its deposit history will be removed.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                repository.delete(userId, g)
                            } catch (_: Exception) {
                                return@launch
                            }
                            if (expandedGoalId == g.id) expandedGoalId = null
                            goalPendingDelete = null
                        }
                    },
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { goalPendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ContributionRow(
    contribution: SavingsContributionEntity,
    currencyCode: String,
    hide: Boolean,
    dateFmt: DateTimeFormatter,
    zone: ZoneId,
    onRemove: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                Instant.ofEpochMilli(contribution.createdAtMillis).atZone(zone).format(dateFmt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Deposit", style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            "+${MoneyFormat.format(currencyCode, contribution.amountCents, hide)}",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF43A047),
        )
        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove deposit")
            }
        }
    }
}

@Composable
private fun GoalAddMoneyRow(
    goal: SavingsGoalEntity,
    profile: UserProfileEntity,
    userId: String,
    repository: SavingsRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    dismissKeyboard: () -> Unit,
) {
    var addAmount by remember(goal.id) { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = addAmount,
            onValueChange = { addAmount = it },
            label = { Text("Add to goal") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                val cents = addAmount.replace(",", ".").toBigDecimalOrNull()
                    ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()
                    ?: return@Button
                dismissKeyboard()
                scope.launch {
                    try {
                        repository.addContribution(userId, goal, cents)
                    } catch (_: Exception) {
                        return@launch
                    }
                    addAmount = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Add money") }
    }
}
