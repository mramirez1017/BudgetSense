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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.amdevstudio.budgetsense.data.local.entity.BudgetCategoryCapEntity
import com.amdevstudio.budgetsense.data.local.entity.BudgetPlanEntity
import com.amdevstudio.budgetsense.data.repository.BudgetRepository
import com.amdevstudio.budgetsense.domain.Categories
import com.amdevstudio.budgetsense.domain.MoneyFormat
import com.amdevstudio.budgetsense.ui.components.GlassCard
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.SectionHeader
import com.amdevstudio.budgetsense.ui.util.appBottomBarSafePadding
import com.amdevstudio.budgetsense.ui.util.rememberKeyboardDismiss
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlannerScreen(
    monthKey: String,
    plan: BudgetPlanEntity?,
    caps: List<BudgetCategoryCapEntity>,
    repository: BudgetRepository,
    currencyCode: String,
) {
    val scope = rememberCoroutineScope()
    val dismissKeyboard = rememberKeyboardDismiss()
    var total by remember(plan) {
        mutableStateOf(
            plan?.totalBudgetCents?.let {
                BigDecimal(it).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }.orEmpty(),
        )
    }
    var daily by remember(plan) {
        mutableStateOf(
            plan?.dailyLimitCents?.let {
                BigDecimal(it).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }.orEmpty(),
        )
    }
    var weekly by remember(plan) {
        mutableStateOf(
            plan?.weeklyLimitCents?.let {
                BigDecimal(it).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }.orEmpty(),
        )
    }

    var cat by remember { mutableStateOf(Categories.expense.first()) }
    var catAmount by remember { mutableStateOf("") }
    var catMenu by remember { mutableStateOf(false) }

    LaunchedEffect(plan) {
        total = plan?.totalBudgetCents?.let {
            BigDecimal(it).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }.orEmpty()
        daily = plan?.dailyLimitCents?.let {
            BigDecimal(it).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }.orEmpty()
        weekly = plan?.weeklyLimitCents?.let {
            BigDecimal(it).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }.orEmpty()
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .appBottomBarSafePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            OverlineCaps("Budget", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text("Plan for $monthKey", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = "Monthly plan")
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it },
                    label = { Text("Monthly budget total") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = daily,
                    onValueChange = { daily = it },
                    label = { Text("Daily spending limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = weekly,
                    onValueChange = { weekly = it },
                    label = { Text("Weekly spending limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    shape = MaterialTheme.shapes.extraLarge,
                    onClick = {
                        dismissKeyboard()
                        scope.launch {
                            val totalCents = total.toCentsOrNull()
                            val dailyCents = daily.toCentsOrNull()
                            val weeklyCents = weekly.toCentsOrNull()
                            repository.savePlan(
                                BudgetPlanEntity(
                                    monthKey = monthKey,
                                    totalBudgetCents = totalCents,
                                    dailyLimitCents = dailyCents,
                                    weeklyLimitCents = weeklyCents,
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save monthly limits", style = MaterialTheme.typography.titleMedium) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Per-category limits", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(expanded = catMenu, onExpandedChange = { catMenu = !catMenu }) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = cat,
                onValueChange = {},
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catMenu) },
            )
            DropdownMenu(expanded = catMenu, onDismissRequest = { catMenu = false }) {
                Categories.expense.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                            cat = c
                            catMenu = false
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = catAmount,
            onValueChange = { catAmount = it },
            label = { Text("Category cap") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            shape = MaterialTheme.shapes.medium,
            onClick = {
                val cents = catAmount.toCentsOrNull() ?: return@Button
                dismissKeyboard()
                scope.launch {
                    repository.saveCategoryCap(
                        BudgetCategoryCapEntity(monthKey = monthKey, category = cat, capCents = cents),
                    )
                    catAmount = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Add / update category cap") }

        if (caps.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            caps.forEach { cap ->
                Text(
                    "${cap.category}: ${MoneyFormat.format(currencyCode, cap.capCents, hide = false)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun String.toCentsOrNull(): Long? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null
    return trimmed.replace(",", ".").toBigDecimalOrNull()
        ?.multiply(BigDecimal(100))
        ?.setScale(0, RoundingMode.HALF_UP)
        ?.toLong()
}
