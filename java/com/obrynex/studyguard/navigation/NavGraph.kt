package com.obrynex.studyguard.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.obrynex.studyguard.LocalViewModelFactory
import com.obrynex.studyguard.booksummarizer.BookSummarizerScreen
import com.obrynex.studyguard.booksummarizer.BookSummarizerViewModel
import com.obrynex.studyguard.data.prefs.PrefsManager
import com.obrynex.studyguard.di.ServiceLocator
import com.obrynex.studyguard.islamic.ui.IslamicScreen
import com.obrynex.studyguard.islamic.ui.IslamicViewModel
import com.obrynex.studyguard.learningmaterials.LearningMaterialsScreen
import com.obrynex.studyguard.learningmaterials.LearningMaterialsViewModel
import com.obrynex.studyguard.summarizer.ui.SummarizerScreen
import com.obrynex.studyguard.summarizer.ui.SummarizerViewModel
import com.obrynex.studyguard.timer.TimerScreen
import com.obrynex.studyguard.timer.TimerViewModel
import com.obrynex.studyguard.tracker.SessionDetailScreen
import com.obrynex.studyguard.tracker.SessionDetailViewModel
import com.obrynex.studyguard.tracker.TrackerScreen
import com.obrynex.studyguard.tracker.TrackerViewModel
import com.obrynex.studyguard.ui.adaptive.shouldUseNavigationRail
import com.obrynex.studyguard.ui.adaptive.contentHorizontalPadding
import com.obrynex.studyguard.ui.onboarding.OnboardingScreen
import com.obrynex.studyguard.ui.theme.*
import com.obrynex.studyguard.wellbeing.WellbeingScreen
import com.obrynex.studyguard.wellbeing.WellbeingViewModel
import kotlinx.coroutines.launch

/* ─── Bottom nav items ──────────────────────────────────────────────────── */

enum class BottomNavItem(
    val route: String,
    val label: String,
    val compactLabel: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TIMER(
        route = "timer",
        label = "Timer",
        compactLabel = "Timer",
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    ),
    LEARNING(
        route = "learning",
        label = "Materials",
        compactLabel = "Learn",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    ),
    SUMMARIZER(
        route = "summarizer",
        label = "Summarizer",
        compactLabel = "AI",
        selectedIcon = Icons.Filled.AutoStories,
        unselectedIcon = Icons.Outlined.Article
    ),
    TRACKER(
        route = "tracker",
        label = "Tracker",
        compactLabel = "Track",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Filled.BarChart
    ),
    MORE(
        route = "more",
        label = "More",
        compactLabel = "More",
        selectedIcon = Icons.Filled.MoreHoriz,
        unselectedIcon = Icons.Outlined.MoreHoriz
    );

    companion object {
        val entriesList = listOf(TIMER, LEARNING, SUMMARIZER, TRACKER, MORE)
    }
}

/* ─── NavGraph ──────────────────────────────────────────────────────────── */

@Composable
fun NavGraph(windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass) {
    val navCtrl = rememberNavController()
    val backStack by navCtrl.currentBackStackEntryAsState()
    val current = backStack?.destination
    val useRail = windowSizeClass.shouldUseNavigationRail
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val useCompactBottomLabels =
        windowSizeClass.widthSizeClass ==
            androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact &&
            screenWidthDp <= 360
    val showTopLevelNavigation = current == null || BottomNavItem.entriesList.any { item ->
        current.hierarchy.any { it.route == item.route }
    }

    CompositionLocalProvider(
        LocalViewModelFactory provides com.obrynex.studyguard.ViewModelFactory.fromApplication()
    ) {
        val onboardingScope = androidx.compose.runtime.rememberCoroutineScope()
        val appContext = LocalContext.current
        val onboardingDone by PrefsManager.onboardingDone(appContext)
            .collectAsStateWithLifecycle(initialValue = false)

        if (!onboardingDone) {
            OnboardingScreen(
                onComplete = {
                    onboardingScope.launch {
                        PrefsManager.setOnboardingDone(appContext)
                    }
                }
            )
            return@CompositionLocalProvider
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BgDark,
            bottomBar = {
                if (!useRail && showTopLevelNavigation) {
                    NavigationBar(
                        containerColor = Surface1,
                        tonalElevation = 0.dp,
                        // Explicitly consume navigation bar insets so the bar sits
                        // flush against the system nav area on all gesture / button
                        // navigation configurations.
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        BottomNavItem.entriesList.forEach { item ->
                            val selected = current?.hierarchy?.any { it.route == item.route } == true

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        if (useCompactBottomLabels) item.compactLabel else item.label,
                                        fontSize = if (useCompactBottomLabels) 9.sp else 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                alwaysShowLabel = !useCompactBottomLabels,
                                selected = selected,
                                onClick = {
                                    navCtrl.navigate(item.route) {
                                        popUpTo(navCtrl.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AccentGreen,
                                    selectedTextColor = AccentGreen,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = AccentGreen.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && showTopLevelNavigation) {
                    NavigationRail(
                        containerColor = Surface1,
                        // Consume only the start + vertical system bars so the rail
                        // accounts for status-bar height at the top and the side
                        // system inset, but doesn't add unneeded bottom padding.
                        windowInsets = WindowInsets.systemBars.only(
                            WindowInsetsSides.Start + WindowInsetsSides.Vertical
                        )
                    ) {
                        Spacer(Modifier.height(12.dp))
                        BottomNavItem.entriesList.forEach { item ->
                            val selected = current?.hierarchy?.any { it.route == item.route } == true

                            NavigationRailItem(
                                icon = {
                                    Icon(
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        item.label,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    navCtrl.navigate(item.route) {
                                        popUpTo(navCtrl.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = AccentGreen,
                                    selectedTextColor = AccentGreen,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = AccentGreen.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }

                NavHost(
                    navController = navCtrl,
                    startDestination = BottomNavItem.TIMER.route,
                    modifier = Modifier.weight(1f)
                ) {
                    // ── Timer ──────────────────────────────────────────────
                    composable(BottomNavItem.TIMER.route) {
                        val vm: TimerViewModel = viewModel(factory = LocalViewModelFactory.current)
                        TimerScreen(
                            vm = vm,
                            onNavigateToTracker = {
                                navCtrl.navigate(BottomNavItem.TRACKER.route) {
                                    popUpTo(navCtrl.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // ── Learning Materials ─────────────────────────────────
                    composable(BottomNavItem.LEARNING.route) {
                        val vm: LearningMaterialsViewModel = viewModel(factory = LocalViewModelFactory.current)
                        LearningMaterialsScreen(vm = vm, windowSizeClass = windowSizeClass)
                    }

                    // ── Summarizer ─────────────────────────────────────────
                    composable(BottomNavItem.SUMMARIZER.route) {
                        val vm: SummarizerViewModel = viewModel(factory = LocalViewModelFactory.current)
                        SummarizerScreen(vm = vm, windowSizeClass = windowSizeClass)
                    }

                    // ── Tracker ────────────────────────────────────────────
                    composable(BottomNavItem.TRACKER.route) {
                        val vm: TrackerViewModel = viewModel(factory = LocalViewModelFactory.current)
                        TrackerScreen(
                            vm = vm,
                            windowSizeClass = windowSizeClass,
                            onNavigateToDetail = { id ->
                                navCtrl.navigate("session/$id")
                            },
                            onNavigateToAI = {
                                navCtrl.navigate("aitutor") { launchSingleTop = true }
                            },
                            onNavigateToHadith = {
                                navCtrl.navigate("hadith") { launchSingleTop = true }
                            },
                            onNavigateToBookSummarizer = {
                                navCtrl.navigate("booksummarizer") { launchSingleTop = true }
                            },
                            onNavigateToWellbeing = {
                                navCtrl.navigate("wellbeing") { launchSingleTop = true }
                            },
                            onNavigateToDebug = {
                                navCtrl.navigate("debug") { launchSingleTop = true }
                            }
                        )
                    }

                    // ── More ───────────────────────────────────────────────
                    composable(BottomNavItem.MORE.route) {
                        MoreScreen(
                            onNavigateToHadith = {
                                navCtrl.navigate("hadith") { launchSingleTop = true }
                            },
                            onNavigateToBookSummarizer = {
                                navCtrl.navigate("booksummarizer") { launchSingleTop = true }
                            },
                            onNavigateToWellbeing = {
                                navCtrl.navigate("wellbeing") { launchSingleTop = true }
                            },
                            onNavigateToDebug = {
                                navCtrl.navigate("debug") { launchSingleTop = true }
                            },
                            windowSizeClass = windowSizeClass
                        )
                    }

                    // ── AI Tutor ───────────────────────────────────────────
                    composable("aitutor") {
                        val vm: com.obrynex.studyguard.aitutor.AiTutorViewModel =
                            viewModel(factory = LocalViewModelFactory.current)
                        com.obrynex.studyguard.aitutor.AiTutorScreen(
                            vm = vm,
                            windowSizeClass = windowSizeClass
                        )
                    }

                    // ── Session Detail ─────────────────────────────────────
                    composable("session/{sessionId}") { entry ->
                        val id = entry.arguments?.getString("sessionId")?.toLongOrNull() ?: return@composable
                        val vm = remember {
                            SessionDetailViewModel(dao = ServiceLocator.studySessionDao)
                        }
                        val session by vm.sessionById(id).collectAsStateWithLifecycle(initialValue = null)
                        session?.let { sess ->
                            SessionDetailScreen(
                                session = sess,
                                onBack = { navCtrl.popBackStack() },
                                onDelete = {
                                    vm.delete(sess)
                                    navCtrl.popBackStack()
                                }
                            )
                        }
                    }

                    // ── Hadith ─────────────────────────────────────────────
                    composable("hadith") {
                        val vm: IslamicViewModel = viewModel(factory = LocalViewModelFactory.current)
                        IslamicScreen(vm = vm, onBack = { navCtrl.popBackStack() })
                    }

                    // ── Book Summarizer ────────────────────────────────────
                    composable("booksummarizer") {
                        val vm: BookSummarizerViewModel = viewModel(factory = LocalViewModelFactory.current)
                        BookSummarizerScreen(vm = vm, onBack = { navCtrl.popBackStack() })
                    }

                    // ── Digital Wellbeing ──────────────────────────────────
                    composable("wellbeing") {
                        val vm: WellbeingViewModel = viewModel(factory = LocalViewModelFactory.current)
                        WellbeingScreen(
                            vm = vm,
                            windowSizeClass = windowSizeClass,
                            onBack = { navCtrl.popBackStack() }
                        )
                    }

                    // ── Debug ──────────────────────────────────────────────
                    composable("debug") {
                        val vm = remember {
                            com.obrynex.studyguard.debug.DebugInfoViewModel(
                                manager   = ServiceLocator.aiEngineManager,
                                hashCache = ServiceLocator.modelHashCache,
                                context   = ServiceLocator.context
                            )
                        }
                        com.obrynex.studyguard.debug.DebugInfoScreen(vm = vm)
                    }
                }
            }
        }
    }
}

/* ─── More Screen ──────────────────────────────────────────────────────── */

@Composable
private fun MoreScreen(
    onNavigateToHadith: () -> Unit,
    onNavigateToBookSummarizer: () -> Unit,
    onNavigateToWellbeing: () -> Unit,
    onNavigateToDebug: () -> Unit,
    windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass? = null
) {
    val hPad = windowSizeClass?.contentHorizontalPadding ?: 16.dp
    val isExpanded = windowSizeClass?.widthSizeClass ==
        androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Expanded

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (isExpanded) Modifier.widthIn(max = 600.dp)
                    else Modifier.fillMaxWidth()
                )
                .padding(horizontal = hPad, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        Text(
            "More",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        Spacer(Modifier.height(8.dp))

        MoreItem(
            icon = Icons.Default.Star,
            title = "Daily Hadith",
            subtitle = "Inspirational sayings",
            onClick = onNavigateToHadith
        )
        MoreItem(
            icon = Icons.Default.Book,
            title = "Book Summarizer",
            subtitle = "Import and summarize long texts",
            onClick = onNavigateToBookSummarizer
        )
        MoreItem(
            icon = Icons.Default.PhoneAndroid,
            title = "Digital Wellbeing",
            subtitle = "Monitor screen time",
            onClick = onNavigateToWellbeing
        )
        MoreItem(
            icon = Icons.Default.BugReport,
            title = "Debug Info",
            subtitle = "AI model diagnostics",
            onClick = onNavigateToDebug
        )
        } // end Column
    } // end Box
}

@Composable
private fun MoreItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(AccentGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = TextMuted.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}
