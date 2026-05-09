package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.BuildConfig
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        BudgetSenseAmbientBackground(Modifier.fillMaxSize())
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                TopAppBar(
                    title = { Text("About") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        ScreenHelpIconButton(title = "About BudgetSense") {
                            Text(
                                "BudgetSense helps you track income and spending, set monthly budgets, save toward goals, and stay on top of bills. Sign in if you’d like profile and activity to stay available across devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
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
                OverlineCaps("BudgetSense", color = MaterialTheme.colorScheme.primary)
                Text("About this app", style = MaterialTheme.typography.headlineSmall)
                NeoPanel(borderAlpha = 0.28f) {
                    Text("Version", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("What’s new", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    val lines = listOf(
                        "Home Savings: circular progress rings per goal—total saved in the center plus % toward your target",
                        "Home Savings layout simplified (goal-focused progress strip; full history stays on the Savings tab)",
                        "Insights: savings-aware tips—for example deposits this month vs last, strongest month so far, and largest single deposit (respects Hide balance)",
                        "Bottom navigation can scroll sideways on small screens so every tab—including Account—is easy to reach",
                        "Global month picker on Home (same month rolls through Money & Budget)",
                        "Savings goals on Home carousel + dedicated Savings tab for goals and deposits",
                        "Bill reminders with local notifications and delete support",
                        "Light-themed UI polish across dashboards, charts, and screens",
                    )
                    lines.forEach { line ->
                        Text(
                            "• $line",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
                NeoPanel(borderAlpha = 0.28f) {
                    Text("Developer", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("AM Dev Studio", style = MaterialTheme.typography.bodyLarge)
                }
                TextButton(onClick = onOpenFaq, modifier = Modifier.fillMaxWidth()) {
                    Text("Open FAQ")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
