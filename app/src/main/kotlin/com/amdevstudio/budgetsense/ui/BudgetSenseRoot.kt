package com.amdevstudio.budgetsense.ui

import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amdevstudio.budgetsense.data.repository.AuthRepository
import com.amdevstudio.budgetsense.data.repository.BillRepository
import com.amdevstudio.budgetsense.data.repository.BudgetRepository
import com.amdevstudio.budgetsense.data.repository.ProfileRepository
import com.amdevstudio.budgetsense.data.repository.SavingsRepository
import com.amdevstudio.budgetsense.data.repository.TransactionRepository
import com.amdevstudio.budgetsense.domain.SavingsMonthSnapshot
import com.amdevstudio.budgetsense.domain.Time
import com.amdevstudio.budgetsense.domain.buildSavingsMonthSnapshot
import com.amdevstudio.budgetsense.ui.screens.BillsScreen
import com.amdevstudio.budgetsense.ui.screens.BudgetPlannerScreen
import com.amdevstudio.budgetsense.ui.screens.DashboardScreen
import com.amdevstudio.budgetsense.ui.screens.InsightsScreen
import com.amdevstudio.budgetsense.ui.screens.LoginScreen
import com.amdevstudio.budgetsense.ui.screens.ProfileSetupScreen
import com.amdevstudio.budgetsense.ui.screens.SavingsScreen
import com.amdevstudio.budgetsense.ui.screens.AboutScreen
import com.amdevstudio.budgetsense.ui.screens.CurrencyConverterScreen
import com.amdevstudio.budgetsense.ui.screens.FaqScreen
import com.amdevstudio.budgetsense.ui.screens.SettingsScreen
import com.amdevstudio.budgetsense.ui.screens.TransactionEditScreen
import com.amdevstudio.budgetsense.ui.screens.TransactionsScreen
import com.amdevstudio.budgetsense.ui.components.BudgetSenseAmbientBackground
import com.amdevstudio.budgetsense.ui.components.OverlineCaps
import com.amdevstudio.budgetsense.ui.util.rememberNetworkAvailable
import com.amdevstudio.budgetsense.ui.util.userFacingMessage
import com.amdevstudio.budgetsense.data.local.entity.BudgetCategoryCapEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import com.amdevstudio.budgetsense.data.local.entity.BudgetPlanEntity
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity
import com.amdevstudio.budgetsense.data.local.entity.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.fragment.app.FragmentActivity

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

/** Clears the back stack; `popUpTo(0)` is invalid and crashes (0 is not a destination id). */
private fun NavHostController.navigateToLoginReplacingBackStack() {
    navigate("login") {
        popUpTo(graph.id) { inclusive = true }
    }
}

@Composable
fun BudgetSenseRoot(
    authRepository: AuthRepository,
    profileRepository: ProfileRepository,
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    billRepository: BillRepository,
    savingsRepository: SavingsRepository,
    appPrefs: SharedPreferences,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val authUser by authRepository.authState()
        .collectAsStateWithLifecycle(initialValue = FirebaseAuth.getInstance().currentUser)

    val networkAvailable by rememberNetworkAvailable()

    LaunchedEffect(authUser?.uid, networkAvailable) {
        val uid = authUser?.uid ?: return@LaunchedEffect
        if (!networkAvailable) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                transactionRepository.syncWithCloud(uid)
                savingsRepository.syncWithCloud(uid)
                billRepository.syncWithCloud(uid)
            } catch (_: Exception) {
                // Offline or Firestore errors; local Room data still works
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) { padding ->
        // App Lock state (stored in SharedPreferences)
        val appLockKey = "app_lock_enabled"
        var appLockEnabled by remember {
            mutableStateOf(appPrefs.getBoolean(appLockKey, false))
        }
        DisposableEffect(appPrefs) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                if (key == appLockKey) appLockEnabled = p.getBoolean(appLockKey, false)
            }
            appPrefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose { appPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

        var unlockedThisSession by rememberSaveable { mutableStateOf(false) }

        // Re-lock whenever app returns to foreground.
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner, appLockEnabled) {
            val observer = LifecycleEventObserver { _, event ->
                if (!appLockEnabled) return@LifecycleEventObserver
                when (event) {
                    Lifecycle.Event.ON_START -> unlockedThisSession = false
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        if (appLockEnabled && !unlockedThisSession) {
            AppLockScreen(
                activity = activity,
                onUnlocked = { unlockedThisSession = true },
            )
            return@Scaffold
        }

        // Collect above NavHost so flows stay active on transaction_edit / bills / etc.
        // (MainShell is not composed when those screens are shown; in-shell collection missed Room updates.)
        var monthKey by rememberSaveable { mutableStateOf(Time.monthKey()) }
        val uid = authUser?.uid
        val profileFlow = remember(uid) {
            if (uid == null) flowOf<UserProfileEntity?>(null) else profileRepository.observe(uid)
        }
        val profile by profileFlow.collectAsStateWithLifecycle(initialValue = null)
        val allTxsFlow = remember(uid) {
            if (uid == null) flowOf(emptyList<TransactionEntity>()) else transactionRepository.observeAll(uid)
        }
        val allTxs by allTxsFlow.collectAsStateWithLifecycle(initialValue = emptyList<TransactionEntity>())
        val monthTotalsFlow = remember(uid, monthKey) {
            if (uid == null) flowOf(0L to 0L) else transactionRepository.observeMonthTotals(monthKey, uid)
        }
        val monthTotals by monthTotalsFlow.collectAsStateWithLifecycle(initialValue = 0L to 0L)
        val monthTxsFlow = remember(uid, monthKey) {
            if (uid == null) flowOf(emptyList<TransactionEntity>()) else transactionRepository.observeMonth(monthKey, uid)
        }
        val monthTxs by monthTxsFlow.collectAsStateWithLifecycle(initialValue = emptyList<TransactionEntity>())
        val budgetPlanFlow = remember(uid, monthKey) {
            if (uid == null) flowOf<BudgetPlanEntity?>(null) else budgetRepository.observePlan(monthKey)
        }
        val budgetPlan by budgetPlanFlow.collectAsStateWithLifecycle(initialValue = null)
        val budgetCapsFlow = remember(uid, monthKey) {
            if (uid == null) flowOf(emptyList<BudgetCategoryCapEntity>()) else budgetRepository.observeCategoryCaps(monthKey)
        }
        val budgetCaps by budgetCapsFlow.collectAsStateWithLifecycle(initialValue = emptyList<BudgetCategoryCapEntity>())
        val savingsGoalsFlow = remember(uid, savingsRepository) {
            if (uid == null) flowOf(emptyList<SavingsGoalEntity>()) else savingsRepository.observeAll(uid)
        }
        val savingsGoals by savingsGoalsFlow.collectAsStateWithLifecycle(initialValue = emptyList<SavingsGoalEntity>())
        val savingsContribsFlow = remember(uid, savingsRepository) {
            if (uid == null) flowOf(emptyList<SavingsContributionEntity>()) else savingsRepository.observeAllContributions(uid)
        }
        val savingsContribs by savingsContribsFlow.collectAsStateWithLifecycle(initialValue = emptyList<SavingsContributionEntity>())
        val savingsSnapshot = remember(savingsGoals, savingsContribs) {
            // monthKey affects savedThisMonth, but we compute selected-month savings separately for the dashboard.
            buildSavingsMonthSnapshot(savingsGoals, savingsContribs, Time.monthKey())
        }

        val selectedMonthStart = remember(monthKey) { Time.startOfMonthMillis(monthKey) }
        val selectedMonthEnd = remember(monthKey) { Time.endOfMonthMillis(monthKey) }
        val selectedMonthSavingsCents = remember(savingsContribs, selectedMonthStart, selectedMonthEnd) {
            savingsContribs
                .asSequence()
                .filter { it.createdAtMillis >= selectedMonthStart && it.createdAtMillis < selectedMonthEnd }
                .sumOf { it.amountCents }
        }
        val previousMonthsSavingsCents = remember(savingsContribs, selectedMonthStart) {
            savingsContribs
                .asSequence()
                .filter { it.createdAtMillis < selectedMonthStart }
                .sumOf { it.amountCents }
        }

        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(animationSpec = tween(190, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(animationSpec = tween(190, easing = FastOutSlowInEasing)) { it / 14 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(150)) +
                    slideOutHorizontally(animationSpec = tween(150)) { -it / 20 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(190, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(animationSpec = tween(190, easing = FastOutSlowInEasing)) { -it / 14 }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(150)) +
                    slideOutHorizontally(animationSpec = tween(150)) { it / 20 }
            },
        ) {
            composable("splash") {
                LaunchedEffect(authUser?.uid) {
                    val user = authUser
                    if (user == null) {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                        return@LaunchedEffect
                    }
                    try {
                        profileRepository.ensureLocalUser(
                            uid = user.uid,
                            fallbackName = user.displayName ?: user.email?.substringBefore("@").orEmpty(),
                        )
                        val profile = profileRepository.observe(user.uid).first()
                            ?: profileRepository.getLocal(user.uid)
                        if (profile?.onboardingComplete == true) {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("profile_setup") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } catch (_: Exception) {
                        navController.navigate("profile_setup") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
                Box(Modifier.fillMaxSize().statusBarsPadding()) {
                    BudgetSenseAmbientBackground(Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            composable("login") {
                LoginScreen(
                    authRepository = authRepository,
                    onSignedIn = {
                        scope.launch {
                            try {
                                val user = FirebaseAuth.getInstance().currentUser ?: return@launch
                                profileRepository.ensureLocalUser(
                                    user.uid,
                                    user.displayName ?: user.email?.substringBefore("@").orEmpty(),
                                )
                                val profile = profileRepository.observe(user.uid).first()
                                    ?: profileRepository.getLocal(user.uid)
                                if (profile?.onboardingComplete == true) {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("profile_setup") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    e.userFacingMessage(
                                        fallback = "Could not load profile",
                                        extraWhenOffline = "Data on this device still works; cloud sync resumes when you're online.",
                                    ),
                                )
                            }
                        }
                    },
                    onError = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    },
                )
            }

            composable("profile_setup") {
                val user = authUser
                if (user == null) {
                    LaunchedEffect(Unit) {
                        navController.navigateToLoginReplacingBackStack()
                    }
                    return@composable
                }
                val profile by profileRepository.observe(user.uid)
                    .collectAsStateWithLifecycle(initialValue = null)
                val p = profile
                if (p == null) {
                    Box(Modifier.fillMaxSize()) {
                        BudgetSenseAmbientBackground(Modifier.fillMaxSize())
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    ProfileSetupScreen(
                        initial = p,
                        onSave = { updated ->
                            scope.launch {
                                profileRepository.save(updated)
                                navController.navigate("main") {
                                    popUpTo("profile_setup") { inclusive = true }
                                }
                                withContext(Dispatchers.IO) {
                                    profileRepository.syncProfileToCloud(updated)
                                }
                            }
                        },
                    )
                }
            }

            composable("main") {
                var selectedTab by rememberSaveable { mutableStateOf("dashboard") }
                MainShell(
                    navController = navController,
                    authUser = authUser,
                    authRepository = authRepository,
                    profileRepository = profileRepository,
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository,
                    snackbarHostState = snackbarHostState,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    monthKey = monthKey,
                    onMonthKeyChanged = { monthKey = it },
                    profile = profile,
                    monthTotals = monthTotals,
                    monthTxs = monthTxs,
                    allTxs = allTxs,
                    budgetPlan = budgetPlan,
                    budgetCaps = budgetCaps,
                    savingsSnapshot = savingsSnapshot,
                    selectedMonthSavingsCents = selectedMonthSavingsCents,
                    previousMonthsSavingsCents = previousMonthsSavingsCents,
                    hasSavingsGoals = savingsGoals.isNotEmpty(),
                    appPrefs = appPrefs,
                    onOpenAbout = { navController.navigate("about") },
                    onOpenFaq = { navController.navigate("faq") },
                    onOpenCurrencyConverter = { navController.navigate("currency_converter") },
                )
            }

            composable(
                route = "transaction_edit/{txId}",
                arguments = listOf(navArgument("txId") { type = NavType.StringType }),
            ) { entry ->
                val txId = entry.arguments?.getString("txId") ?: "new"
                val user = authUser
                if (user == null) {
                    LaunchedEffect(Unit) {
                        navController.navigateToLoginReplacingBackStack()
                    }
                } else {
                    TransactionEditScreen(
                        txId = txId,
                        userId = user.uid,
                        repository = transactionRepository,
                        onDone = {
                            if (!navController.popBackStack()) {
                                navController.navigate("main") { launchSingleTop = true }
                            }
                        },
                    )
                }
            }

            composable("bills") {
                val user = authUser
                if (user == null) {
                    LaunchedEffect(Unit) {
                        navController.navigateToLoginReplacingBackStack()
                    }
                } else {
                    val bills by billRepository.observeAll(user.uid).collectAsStateWithLifecycle(initialValue = emptyList())
                    BillsScreen(
                        repository = billRepository,
                        bills = bills,
                        userId = user.uid,
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            composable("savings") {
                val user = authUser
                if (user == null) {
                    LaunchedEffect(Unit) {
                        navController.navigateToLoginReplacingBackStack()
                    }
                } else {
                    val profile by profileRepository.observe(user.uid)
                        .collectAsStateWithLifecycle(initialValue = null)
                    val goals by savingsRepository.observeAll(user.uid).collectAsStateWithLifecycle(initialValue = emptyList())
                    val p = profile
                    if (p == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        SavingsScreen(
                            profile = p,
                            userId = user.uid,
                            repository = savingsRepository,
                            goals = goals,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }

            composable("insights") {
                val user = authUser
                if (user == null) {
                    LaunchedEffect(Unit) {
                        navController.navigateToLoginReplacingBackStack()
                    }
                } else {
                    val p = profile
                    if (p == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        val capMap = remember(budgetCaps) { budgetCaps.associate { it.category to it.capCents } }
                        InsightsScreen(
                            profile = p,
                            allTransactions = allTxs,
                            categoryCaps = capMap,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }

            composable("about") {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFaq = { navController.navigate("faq") },
                )
            }

            composable("faq") {
                FaqScreen(onBack = { navController.popBackStack() })
            }

            composable("currency_converter") {
                val user = authUser
                if (user == null) {
                    LaunchedEffect(Unit) {
                        navController.navigateToLoginReplacingBackStack()
                    }
                } else {
                    val code = profile?.currencyCode
                    if (code == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        CurrencyConverterScreen(
                            profileCurrency = code,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun AppLockScreen(
    activity: FragmentActivity?,
    onUnlocked: () -> Unit,
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    fun launchPrompt() {
        val a = activity ?: run {
            error = "App lock is unavailable on this screen."
            return
        }
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val mgr = BiometricManager.from(context)
        val ok = mgr.canAuthenticate(authenticators)
        if (ok != BiometricManager.BIOMETRIC_SUCCESS) {
            error = "No lock method available. Set up a screen lock (PIN/Pattern/Password) first."
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            a,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    error = null
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    error = errString.toString()
                }

                override fun onAuthenticationFailed() {
                    error = "Not recognized. Try again."
                }
            },
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock BudgetSense")
            .setSubtitle("Use biometrics or your device PIN/pattern/password")
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(info)
    }

    LaunchedEffect(Unit) {
        launchPrompt()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            OverlineCaps("Locked", color = MaterialTheme.colorScheme.primary)
            Text("BudgetSense is locked", style = MaterialTheme.typography.headlineSmall)
            if (error != null) {
                Text(
                    error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = { launchPrompt() }) { Text("Unlock") }
        }
    }
}

@Composable
private fun MainShell(
    navController: androidx.navigation.NavHostController,
    authUser: com.google.firebase.auth.FirebaseUser?,
    authRepository: AuthRepository,
    profileRepository: ProfileRepository,
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    snackbarHostState: SnackbarHostState,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    monthKey: String,
    onMonthKeyChanged: (String) -> Unit,
    profile: UserProfileEntity?,
    monthTotals: Pair<Long, Long>,
    monthTxs: List<TransactionEntity>,
    allTxs: List<TransactionEntity>,
    budgetPlan: BudgetPlanEntity?,
    budgetCaps: List<BudgetCategoryCapEntity>,
    savingsSnapshot: SavingsMonthSnapshot,
    selectedMonthSavingsCents: Long,
    previousMonthsSavingsCents: Long,
    hasSavingsGoals: Boolean,
    appPrefs: SharedPreferences,
    onOpenAbout: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenCurrencyConverter: () -> Unit,
) {
    val user = authUser
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigateToLoginReplacingBackStack()
        }
        return
    }

    val scope = rememberCoroutineScope()
    val categoryCapsMap = remember(budgetCaps) {
        budgetCaps.associate { it.category to it.capCents }
    }

    val tabs = remember {
        listOf(
            Tab("dashboard", "Home", Icons.Default.Home),
            Tab("transactions", "Money", Icons.Default.AccountBalanceWallet),
            Tab("budget", "Budget", Icons.Default.PieChart),
            Tab("settings", "Account", Icons.Default.Person),
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Rounded shell via Surface — avoid Modifier.clip on the bar itself, which can shrink
            // hit targets for the first NavigationBarItem (Home) near the rounded edge.
            Surface(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(36.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                ),
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab.route,
                            onClick = { onTabSelected(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val p = profile
        if (p == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                BudgetSenseAmbientBackground(Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            return@Scaffold
        }

        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding),
        ) {
            BudgetSenseAmbientBackground(Modifier.fillMaxSize())
            when (selectedTab) {
                "dashboard" -> DashboardScreen(
                    profile = p,
                    monthIncome = monthTotals.first,
                    monthExpense = monthTotals.second,
                    monthBudgetCap = budgetPlan?.totalBudgetCents,
                    monthTransactions = monthTxs,
                    monthKey = monthKey,
                    onMonthKeyChanged = onMonthKeyChanged,
                    savingsSnapshot = savingsSnapshot,
                    selectedMonthSavingsCents = selectedMonthSavingsCents,
                    previousMonthsSavingsCents = previousMonthsSavingsCents,
                    hasSavingsGoals = hasSavingsGoals,
                    onOpenTransactions = { onTabSelected("transactions") },
                    onOpenBudget = { onTabSelected("budget") },
                    onOpenBills = { navController.navigate("bills") },
                    onOpenSavings = { navController.navigate("savings") },
                    onOpenInsights = { navController.navigate("insights") },
                )

                "transactions" -> TransactionsScreen(
                    profile = p,
                    transactions = allTxs,
                    monthBudgetCents = budgetPlan?.totalBudgetCents,
                    categoryCaps = categoryCapsMap,
                    monthKey = monthKey,
                    onMonthKeyChanged = onMonthKeyChanged,
                    onAdd = { navController.navigate("transaction_edit/new") },
                    onOpen = { id -> navController.navigate("transaction_edit/$id") },
                    onDelete = { tx ->
                        scope.launch {
                            transactionRepository.delete(user.uid, tx)
                            snackbarHostState.showSnackbar("Transaction deleted")
                        }
                    },
                )

                "budget" -> BudgetPlannerScreen(
                    monthKey = monthKey,
                    plan = budgetPlan,
                    caps = budgetCaps,
                    repository = budgetRepository,
                    currencyCode = p.currencyCode,
                )

                "settings" -> SettingsScreen(
                    profile = p,
                    profileRepository = profileRepository,
                    authRepository = authRepository,
                    transactionRepository = transactionRepository,
                    appPrefs = appPrefs,
                    onOpenAbout = onOpenAbout,
                    onOpenFaq = onOpenFaq,
                    onOpenCurrencyConverter = onOpenCurrencyConverter,
                    onSignedOut = {
                        navController.navigateToLoginReplacingBackStack()
                    },
                )

                else -> {
                    LaunchedEffect(Unit) { onTabSelected("dashboard") }
                    Box(Modifier.fillMaxSize())
                }
            }
        }
    }
}
