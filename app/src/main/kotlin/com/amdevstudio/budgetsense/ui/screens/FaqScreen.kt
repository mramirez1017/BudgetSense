package com.amdevstudio.budgetsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.NeoPanel
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.components.ScreenHelpIconButton

private data class FaqQA(val question: String, val answer: String)

private val faqSections: List<Pair<String, List<FaqQA>>> = listOf(
    "General" to listOf(
        FaqQA(
            "What is BudgetSense for?",
            "BudgetSense is a personal finance companion: see how much you earned and spent this month, allocate money to categories, grow savings goals, and remember bills — all from your phone.",
        ),
        FaqQA(
            "Do I need an account?",
            "You sign in (for example with Google) to keep a profile across devices and use cloud backup. BudgetSense keeps working with your local data when you are temporarily offline.",
        ),
    ),
    "Home" to listOf(
        FaqQA(
            "What does the month selector do?",
            "The month picker on Home is global: it controls which month you’re reviewing on Home, Money, and Budget. You can only pick the current month or past months (no future months).",
        ),
        FaqQA(
            "What does Home show?",
            "Home summarizes the selected month’s income, expenses, and remaining budget. It also shows swipeable savings goal tiles and an Insights button for quick tips.",
        ),
        FaqQA(
            "Why are amounts hidden sometimes?",
            "If “Hide balances” is on under Account, money amounts appear as bullets so you can show the screen to someone else without revealing numbers.",
        ),
    ),
    "Money (transactions)" to listOf(
        FaqQA(
            "How do I add income or spending?",
            "Open the Money tab and tap + to add an entry. Pick income/expense, amount, category, and an optional note. The + button can be dragged if it blocks something on your screen.",
        ),
        FaqQA(
            "How do I edit or delete?",
            "Tap any row in Money to edit. Inside the edit screen you can adjust fields and delete the transaction permanently.",
        ),
        FaqQA(
            "How is the month grouped?",
            "Money shows transactions for the same month you selected on Home.",
        ),
    ),
    "Budget" to listOf(
        FaqQA(
            "What is the monthly budget cap?",
            "A single number BudgetSense compares against expenses for the month. It complements per-category caps (see below).",
        ),
        FaqQA(
            "What are category caps?",
            "You can assign a spending limit per category (for example groceries). Money tab and Home show progress so you notice before you overspend.",
        ),
    ),
    "Savings" to listOf(
        FaqQA(
            "How do savings goals work?",
            "Create a goal with a target amount and optional deadline. Add deposits anytime; BudgetSense totals how far you’ve come versus the target. The + button is draggable.",
        ),
        FaqQA(
            "What do I see on Home vs Savings tab?",
            "Home shows a quick overview and swipeable goal tiles. The Savings tab shows the full list, deposit history, editing, and deleting goals.",
        ),
    ),
    "Bills" to listOf(
        FaqQA(
            "How are bill reminders used?",
            "Add bills with a due date and how many days before you want a reminder. Reminders are scheduled locally on this phone. You can mark a bill as “Paid this month”, and delete bills with the trash icon. The + button is draggable.",
        ),
    ),
    "Insights" to listOf(
        FaqQA(
            "What does Insights summarize?",
            "Insights turns your spending into short tips (week-to-week and category-based). It’s opened from Home.",
        ),
    ),
    "Sync & sign out" to listOf(
        FaqQA(
            "How does sync work?",
            "After sign-in, BudgetSense keeps your profile and transactions in sync with your account in the cloud, so you can continue on another device using the same sign-in. Sync resumes automatically when connectivity returns.",
        ),
        FaqQA(
            "What happens when I sign out?",
            "Your session clears and BudgetSense wipes local databases on this device to protect privacy. Signing in again downloads what’s stored for that account.",
        ),
    ),
    "Privacy" to listOf(
        FaqQA(
            "Who can see my data?",
            "Your data stays on your device and, when you’re signed in, in secure account-linked storage tied to your sign-in — not visible to other users. Protect your Google account password and device lock.",
        ),
    ),
    "Currency & converter" to listOf(
        FaqQA(
            "Changing my currency in Account",
            "Pick your profile currency under Account — this controls how BudgetSense formats numbers everywhere it shows money. Stored amounts stay the same denomination; we do not auto-convert old entries.",
        ),
        FaqQA(
            "What is the Currency converter?",
            "It uses live ECB-based reference rates from Frankfurter (via the Internet) so you can compare two currencies quickly. Rates update when you change inputs or tap refresh — not a forex trading feed.",
        ),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    onBack: () -> Unit,
) {
    val expanded = remember { mutableStateListOf<Boolean>().apply { repeat(faqSections.size) { add(false) } } }
    Box(Modifier.fillMaxSize()) {
        BudgetSenseAmbientBackground(Modifier.fillMaxSize())
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                TopAppBar(
                    title = { Text("FAQ") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        ScreenHelpIconButton(title = "Using this FAQ") {
                            Text(
                                "Answers are grouped by topic. Open a section below to read each question.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "If something looks wrong, check your connection — sync and the currency converter need the Internet.",
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
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                OverlineCaps("Help", color = MaterialTheme.colorScheme.primary)
                Text("Frequently asked questions", style = MaterialTheme.typography.headlineSmall)
                faqSections.forEachIndexed { index, (title, pairs) ->
                    NeoPanel(borderAlpha = 0.28f) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded[index] = !expanded[index] },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                imageVector = if (expanded[index]) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded[index]) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (expanded[index]) {
                            Spacer(Modifier.height(12.dp))
                            pairs.forEachIndexed { qaIndex, qa ->
                                if (qaIndex > 0) {
                                    HorizontalDivider(
                                        Modifier.padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    )
                                }
                                Text(qa.question, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    qa.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
