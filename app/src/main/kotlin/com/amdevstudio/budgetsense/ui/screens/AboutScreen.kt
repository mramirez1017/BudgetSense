package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.BuildConfig
import com.amdevstudio.budgetsense.domain.TipsOfTheDay
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.NeonCalloutCard
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.TipOfTheDayDialog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
) {
    val tipOfTheDay = remember(LocalDate.now().toEpochDay()) { TipsOfTheDay.forToday() }
    var tipDialogOpen by remember { mutableStateOf(false) }
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
                NeonCalloutCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tipDialogOpen = true },
                    accent = MaterialTheme.colorScheme.tertiary,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Tip of the day",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "Tap to read today’s tip",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (tipDialogOpen) {
                    TipOfTheDayDialog(
                        description = tipOfTheDay,
                        onDismiss = { tipDialogOpen = false },
                    )
                }
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
                        "Home tiles: flowing animated gradients on Top expenses and Savings cards",
                        "Home Spending mix: ring colors match the Top expenses tiles, with % labels on the ring",
                        "Tip of the day: new artwork dialog with the tip text fitted inside the speech bubble",
                        "Money: + button default position sits above the floating bottom bar (still draggable)",
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
