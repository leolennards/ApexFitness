@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.apexfitness.ui.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.GamificationCalculations
import com.example.apexfitness.data.PersonalRecord
import com.example.apexfitness.data.Routine
import com.example.apexfitness.data.RoutineIcons
import com.example.apexfitness.data.StatsCalculations
import com.example.apexfitness.data.UserProfile
import com.example.apexfitness.data.WorkoutLog
import com.example.apexfitness.data.todayDayCode
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.notifications.NotificationPreferences
import com.example.apexfitness.ui.notifications.NotificationScheduler
import com.example.apexfitness.ui.theme.ThemePreferences
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassContentColor
import com.example.apexfitness.ui.theme.glassMutedContentColor
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.SkeletonCircle
import com.example.apexfitness.ui.theme.ApexProgressBar
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.LocalMotionEnabled
import com.example.apexfitness.ui.theme.LocalNavAnimatedScope
import com.example.apexfitness.ui.theme.LocalSharedTransitionScope
import com.example.apexfitness.ui.theme.MetricBlock
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.SharedKeys
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.apexSpring
import com.example.apexfitness.ui.theme.apexTween
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberHaptics
import com.example.apexfitness.ui.theme.sharedCardBounds
import com.example.apexfitness.ui.theme.staggeredEntrance
import com.example.apexfitness.ui.settings.SettingsScreen
import com.example.apexfitness.ui.history.ActivityHistoryScreen
import com.example.apexfitness.ui.health.HealthDataScreen
import com.example.apexfitness.ui.Stats.StatsPage
import com.example.apexfitness.ui.Workout.WorkoutTabPage
import com.example.apexfitness.ui.Workout.WorkoutSessionScreen
import com.example.apexfitness.ui.Profile.EditProfileScreen
import com.example.apexfitness.ui.Profile.ProfilePage
import com.example.apexfitness.ui.achievements.AchievementsScreen
import com.example.apexfitness.ui.achievements.AchievementCelebrationOverlay
import com.example.apexfitness.ui.gamification.LevelUpCelebrationOverlay
import com.example.apexfitness.ui.achievements.AchievementTiers
import com.example.apexfitness.ui.calendar.CalendarScreen
import com.example.apexfitness.ui.challenges.ChallengesScreen
import com.example.apexfitness.ui.water.WaterTrackingScreen
import com.example.apexfitness.ui.cardio.CardioTrackingScreen
import com.example.apexfitness.ui.welcome.WelcomeScreen
import kotlinx.coroutines.launch

import com.example.apexfitness.ui.authentication.register.GetStartedScreen
import com.example.apexfitness.ui.authentication.register.FitnessGoalsScreen
import com.example.apexfitness.ui.authentication.register.WorkoutScheduleScreen
import com.example.apexfitness.ui.authentication.login.SignInScreen
import com.example.apexfitness.ui.authentication.SignUpScreen
import com.example.apexfitness.ui.authentication.OnboardingFormState
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.routines.RoutineListScreen
import com.example.apexfitness.ui.routines.CreateEditRoutineScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Android 15+ forces edge-to-edge, so I turn it on and pad the root layout for the system bars
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        // Keep the splash screen up for a moment so the logo can be seen
        val splashStartTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            System.currentTimeMillis() - splashStartTime < 600L
        }
        ThemePreferences.init(applicationContext)
        NotificationPreferences.init(applicationContext)
        NotificationScheduler.createChannel(applicationContext)
        setContent {
            val isDarkMode by ThemePreferences.isDarkMode.collectAsState()
            val notificationsEnabled by NotificationPreferences.enabled.collectAsState()
            val reminderHour by NotificationPreferences.reminderHour.collectAsState()
            val reminderMinute by NotificationPreferences.reminderMinute.collectAsState()
            val appContext = applicationContext
            ApexFitnessTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val onboardingFormState = remember { OnboardingFormState() }
                val startDestination = remember {
                    if (AuthService(applicationContext).getCurrentUser() != null) "main" else "welcome"
                }

                // Screen transitions: fade plus a small slide. Everything is instant if system animations are off.
                val motionEnabled = LocalMotionEnabled.current
                val slidePx = with(LocalDensity.current) { Motion.ScreenSlide.roundToPx() }

                // Solid background behind the NavHost so a fading screen never shows the window background
                SharedTransitionLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                ) {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = {
                                fadeIn(apexTween(motionEnabled, Motion.Standard)) +
                                    slideInHorizontally(apexSpring(motionEnabled)) { slidePx }
                            },
                            exitTransition = {
                                fadeOut(apexTween(motionEnabled, Motion.Micro + 30)) +
                                    slideOutHorizontally(apexSpring(motionEnabled)) { -slidePx }
                            },
                            popEnterTransition = {
                                fadeIn(apexTween(motionEnabled, Motion.Standard)) +
                                    slideInHorizontally(apexSpring(motionEnabled)) { -slidePx }
                            },
                            popExitTransition = {
                                fadeOut(apexTween(motionEnabled, Motion.Micro + 30)) +
                                    slideOutHorizontally(apexSpring(motionEnabled)) { slidePx }
                            }
                        ) {
                            composable("welcome") {
                                WelcomeScreen(navController = navController)
                            }
                            composable("signup") {
                                SignUpScreen(navController = navController, formState = onboardingFormState)
                            }
                            composable("getStarted") {
                                GetStartedScreen(navController = navController, formState = onboardingFormState)
                            }
                            composable("fitnessGoals") {
                                FitnessGoalsScreen(navController = navController, formState = onboardingFormState)
                            }
                            composable("workoutSchedule") {
                                WorkoutScheduleScreen(navController = navController, formState = onboardingFormState)
                            }
                            composable("signin") {
                                SignInScreen(navController = navController)
                            }
                            composable("main") {
                                CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                    MainScreen(navController = navController)
                                }
                            }
                            composable("routines") {
                                CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                    RoutineListScreen(navController = navController)
                                }
                            }
                            composable("routineEditor/{routineId}") { backStackEntry ->
                                val routineId = backStackEntry.arguments?.getString("routineId") ?: "new"
                                CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                    CreateEditRoutineScreen(navController = navController, routineId = routineId)
                                }
                            }
                            composable("workoutSession/{routineId}") { backStackEntry ->
                                val routineId = backStackEntry.arguments?.getString("routineId") ?: return@composable
                                CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                    WorkoutSessionScreen(navController = navController, routineId = routineId)
                                }
                            }
                            composable("editProfile") {
                                EditProfileScreen(navController = navController)
                            }
                            composable("settings") {
                                SettingsScreen(
                                    navController = navController,
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = { enabled -> ThemePreferences.setDarkMode(appContext, enabled) },
                                    notificationsEnabled = notificationsEnabled,
                                    reminderHour = reminderHour,
                                    reminderMinute = reminderMinute,
                                    onToggleNotifications = { enabled -> NotificationPreferences.setEnabled(appContext, enabled) },
                                    onReminderTimeChange = { hour, minute -> NotificationPreferences.setReminderTime(appContext, hour, minute) }
                                )
                            }
                            composable("activityHistory") {
                                ActivityHistoryScreen(navController = navController)
                            }
                            composable("healthData") {
                                HealthDataScreen(navController = navController)
                            }
                            composable("achievements") {
                                AchievementsScreen(navController = navController)
                            }
                            composable("calendar") {
                                CalendarScreen(navController = navController)
                            }
                            composable("challenges") {
                                ChallengesScreen(navController = navController)
                            }
                            composable("waterTracking") {
                                WaterTrackingScreen(navController = navController)
                            }
                            composable("cardio") {
                                CardioTrackingScreen(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Bottom nav items
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Stats : Screen("stats", "Stats", Icons.Outlined.BarChart)
    object Workout : Screen("workout", "Workout", Icons.Outlined.FitnessCenter)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid

    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var logs by remember { mutableStateOf<List<WorkoutLog>>(emptyList()) }
    var personalRecords by remember { mutableStateOf<List<PersonalRecord>>(emptyList()) }
    var todayWaterMl by remember { mutableStateOf(0) }
    var hasLoadedProfile by remember { mutableStateOf(false) }
    var hasLoadedRoutines by remember { mutableStateOf(false) }
    var hasLoadedLogs by remember { mutableStateOf(false) }
    var hasLoadedPersonalRecords by remember { mutableStateOf(false) }
    var hasLoadedWater by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        val id = uid ?: return@LaunchedEffect
        launch { FirestoreRepository.observeProfile(id).collect { profile = it; hasLoadedProfile = true } }
        launch { FirestoreRepository.observeRoutines(id).collect { routines = it; hasLoadedRoutines = true } }
        launch { FirestoreRepository.observeWorkoutLogs(id).collect { logs = it; hasLoadedLogs = true } }
        launch { FirestoreRepository.observePersonalRecords(id).collect { personalRecords = it; hasLoadedPersonalRecords = true } }
        launch {
            FirestoreRepository.observeTodayWaterLog(id).collect {
                todayWaterMl = it?.millilitersConsumed ?: 0
                hasLoadedWater = true
            }
        }
    }

    val isInitialLoading = uid != null && !(hasLoadedProfile && hasLoadedRoutines && hasLoadedLogs && hasLoadedPersonalRecords && hasLoadedWater)

    val coroutineScope = rememberCoroutineScope()
    var dismissedAchievementsThisSession by remember { mutableStateOf<Set<String>>(emptySet()) }
    val acknowledgedAchievementIds = remember(profile, dismissedAchievementsThisSession) {
        (profile?.acknowledgedAchievementIds ?: emptyList()).toSet() + dismissedAchievementsThisSession
    }
    val activeAchievementCelebration = remember(logs.size, acknowledgedAchievementIds, isInitialLoading) {
        if (isInitialLoading) null
        else AchievementTiers
            .filter { logs.size >= it.threshold && it.id !in acknowledgedAchievementIds }
            .minByOrNull { it.threshold }
    }

    fun dismissAchievementCelebration() {
        val tier = activeAchievementCelebration ?: return
        dismissedAchievementsThisSession = dismissedAchievementsThisSession + tier.id
        val currentUid = uid ?: return
        val updatedAcknowledged = ((profile?.acknowledgedAchievementIds ?: emptyList()) + tier.id).distinct()
        coroutineScope.launch {
            runCatching { FirestoreRepository.updateProfileFields(currentUid, mapOf("acknowledgedAchievementIds" to updatedAcknowledged)) }
        }
    }

    val levelProgress = remember(logs) { GamificationCalculations.levelProgress(logs) }
    var dismissedLevelUpThisSession by remember { mutableStateOf(1) }
    val acknowledgedLevel = remember(profile, dismissedLevelUpThisSession) {
        maxOf(profile?.acknowledgedLevel ?: 1, dismissedLevelUpThisSession)
    }
    // Only shown when there is no achievement popup, so they never stack
    val activeLevelUpCelebration = remember(levelProgress.level, acknowledgedLevel, isInitialLoading, activeAchievementCelebration) {
        if (isInitialLoading || activeAchievementCelebration != null) null
        else if (levelProgress.level > acknowledgedLevel) levelProgress.level else null
    }

    fun dismissLevelUpCelebration() {
        val newLevel = activeLevelUpCelebration ?: return
        dismissedLevelUpThisSession = newLevel
        val currentUid = uid ?: return
        coroutineScope.launch {
            runCatching { FirestoreRepository.updateProfileFields(currentUid, mapOf("acknowledgedLevel" to newLevel)) }
        }
    }

    fun goToRoutines() { navController?.navigate("routines") }
    fun startWorkout(routineId: String) { navController?.navigate("workoutSession/$routineId") }

    val glassState = rememberGlassState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = { screen -> selectedScreen = screen },
                glassState = glassState
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // No padding here on purpose. Each page gets bottomContentPadding so nothing hides under the tab bar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassScreenBackground(glassState)
        ) {
            // Tabs just fade, no slide
            Crossfade(
                targetState = selectedScreen,
                animationSpec = motionTween(Motion.Standard),
                label = "tabCrossfade"
            ) { screen ->
                when (screen) {
                    is Screen.Home -> HomePage(
                        profile = profile,
                        routines = routines,
                        logs = logs,
                        todayWaterMl = todayWaterMl,
                        onStartWorkout = ::startWorkout,
                        onManageRoutines = ::goToRoutines,
                        onSeeAllRoutines = { selectedScreen = Screen.Workout },
                        onQuickAddWater = { amount ->
                            uid?.let { id ->
                                coroutineScope.launch { runCatching { FirestoreRepository.addWater(id, amount) } }
                            }
                        },
                        onOpenWaterTracking = { navController?.navigate("waterTracking") },
                        isLoading = isInitialLoading,
                        glassState = glassState,
                        bottomContentPadding = paddingValues.calculateBottomPadding()
                    )
                    is Screen.Stats -> StatsPage(
                        logs = logs,
                        personalRecords = personalRecords,
                        profile = profile,
                        isLoading = isInitialLoading,
                        onOpenCalendar = { navController?.navigate("calendar") },
                        glassState = glassState,
                        bottomContentPadding = paddingValues.calculateBottomPadding()
                    )
                    is Screen.Workout -> WorkoutTabPage(
                        routines = routines,
                        onStart = ::startWorkout,
                        onManageRoutines = ::goToRoutines,
                        isLoading = isInitialLoading,
                        glassState = glassState,
                        bottomContentPadding = paddingValues.calculateBottomPadding()
                    )
                    is Screen.Profile -> ProfilePage(
                        profile = profile,
                        totalWorkouts = logs.size,
                        streak = StatsCalculations.currentStreak(logs),
                        levelProgress = levelProgress,
                        onSignOut = {
                            authService.signOut()
                            navController?.let { nav ->
                                nav.navigate("welcome") {
                                    popUpTo(nav.graph.id) { inclusive = true }
                                }
                            }
                        },
                        onEditProfile = { navController?.navigate("editProfile") },
                        onOpenActivityHistory = { navController?.navigate("activityHistory") },
                        onOpenHealthData = { navController?.navigate("healthData") },
                        onOpenSettings = { navController?.navigate("settings") },
                        onOpenAchievements = { navController?.navigate("achievements") },
                        onOpenChallenges = { navController?.navigate("challenges") },
                        onOpenCardio = { navController?.navigate("cardio") },
                        onShareApp = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "I'm training with ApexFitness - check it out!")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share ApexFitness"))
                        },
                        isLoading = isInitialLoading,
                        glassState = glassState,
                        bottomContentPadding = paddingValues.calculateBottomPadding()
                    )
                }
            }

            activeAchievementCelebration?.let { tier ->
                AchievementCelebrationOverlay(tier = tier, onDismiss = { dismissAchievementCelebration() })
            }
            activeLevelUpCelebration?.let { newLevel ->
                LevelUpCelebrationOverlay(
                    level = newLevel,
                    title = GamificationCalculations.levelTitle(newLevel),
                    onDismiss = { dismissLevelUpCelebration() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    glassState: com.example.apexfitness.ui.theme.GlassState
) {
    val screens = listOf(
        Screen.Home,
        Screen.Stats,
        Screen.Workout,
        Screen.Profile
    )
    val haptics = rememberHaptics()
    val hairline = MaterialTheme.apex.hairline
    val selectedTint = MaterialTheme.apex.accentText
    val mutedTint = MaterialTheme.apex.mutedText

    // Simple tab bar: solid background, one thin line on top, active tab is gold
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color = hairline,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        screens.forEach { screen ->
            val selected = selectedScreen.route == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        haptics.tick()
                        onScreenSelected(screen)
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(text = screen.title, style = MaterialTheme.typography.labelSmall)
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedTint,
                    selectedTextColor = selectedTint,
                    unselectedIconColor = mutedTint,
                    unselectedTextColor = mutedTint,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// Home: today's workout first, then the smaller info. Shows a skeleton while loading.
@Composable
fun HomePage(
    profile: UserProfile?,
    routines: List<Routine>,
    logs: List<WorkoutLog>,
    todayWaterMl: Int = 0,
    onStartWorkout: (String) -> Unit,
    onManageRoutines: () -> Unit,
    onSeeAllRoutines: () -> Unit,
    onQuickAddWater: (Int) -> Unit = {},
    onOpenWaterTracking: () -> Unit = {},
    isLoading: Boolean = false,
    glassState: com.example.apexfitness.ui.theme.GlassState = rememberGlassState(),
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = motionTween(Motion.Standard),
        label = "homeLoadingCrossfade"
    ) { loading ->
        if (loading) {
            HomeSkeleton(bottomContentPadding = bottomContentPadding)
        } else {
            HomeContent(
                profile = profile,
                routines = routines,
                logs = logs,
                todayWaterMl = todayWaterMl,
                onStartWorkout = onStartWorkout,
                onManageRoutines = onManageRoutines,
                onSeeAllRoutines = onSeeAllRoutines,
                onQuickAddWater = onQuickAddWater,
                onOpenWaterTracking = onOpenWaterTracking,
                glassState = glassState,
                bottomContentPadding = bottomContentPadding
            )
        }
    }
}

@Composable
private fun HomeContent(
    profile: UserProfile?,
    routines: List<Routine>,
    logs: List<WorkoutLog>,
    todayWaterMl: Int,
    onStartWorkout: (String) -> Unit,
    onManageRoutines: () -> Unit,
    onSeeAllRoutines: () -> Unit,
    onQuickAddWater: (Int) -> Unit,
    onOpenWaterTracking: () -> Unit,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    bottomContentPadding: androidx.compose.ui.unit.Dp
) {
    val today = remember { todayDayCode() }
    val todaysRoutines = remember(routines) { routines.filter { it.days.contains(today) } }
    val streak = remember(logs) { StatsCalculations.currentStreak(logs) }
    val totalWorkouts = logs.size
    val weeklyProgress = remember(logs, profile) {
        StatsCalculations.weeklyCompletionPercent(logs, profile?.scheduleDays?.size ?: 7)
    }
    val firstName = profile?.name?.trim()?.substringBefore(" ").takeUnless { it.isNullOrBlank() } ?: "there"
    val initial = profile?.name?.trim()?.take(1)?.uppercase()
    val greeting = remember { greetingForNow() }
    val motionEnabled = LocalMotionEnabled.current
    val shownRoutines = routines.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space3,
            bottom = Dimens.Space3 + bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        item(key = "header") {
            HomeHeader(
                greeting = greeting,
                dayName = fullDayNameFromCode(today),
                firstName = firstName,
                initial = initial,
                modifier = Modifier.staggeredEntrance(index = 0, key = "home-header")
            )
        }

        // Today's workout, or an empty state
        if (todaysRoutines.isEmpty()) {
            item(key = "hero-empty") {
                NoRoutineTodayCard(
                    modifier = Modifier.staggeredEntrance(index = 1, key = "home-hero-empty"),
                    glassState = glassState,
                    onCreateRoutine = onManageRoutines
                )
            }
        } else {
            items(todaysRoutines, key = { "hero-${it.id}" }) { routine ->
                TodaysChallengeCard(
                    routine = routine,
                    glassState = glassState,
                    onStart = { onStartWorkout(routine.id) },
                    modifier = Modifier.staggeredEntrance(index = 1, key = "home-hero-${routine.id}")
                )
            }
        }

        item(key = "metrics") {
            HomeMetricsCard(
                streak = streak,
                totalWorkouts = totalWorkouts,
                weeklyPercent = weeklyProgress,
                glassState = glassState,
                modifier = Modifier.staggeredEntrance(index = 2, key = "home-metrics")
            )
        }

        item(key = "water") {
            WaterWidgetCard(
                todayMl = todayWaterMl,
                goalMl = profile?.dailyWaterGoalMl ?: 2500,
                glassState = glassState,
                onQuickAdd = { onQuickAddWater(250) },
                onClick = onOpenWaterTracking,
                modifier = Modifier.staggeredEntrance(index = 3, key = "home-water")
            )
        }

        item(key = "routines-header") {
            HomeSectionHeader(
                title = "My Routines",
                subtitle = "Built by you",
                actionLabel = "See all",
                onAction = onSeeAllRoutines,
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 4, key = "home-routines-header")
            )
        }

        if (routines.isEmpty()) {
            item(key = "routines-empty") {
                NoRoutinesYetCard(
                    modifier = Modifier.staggeredEntrance(index = 5, key = "home-routines-empty"),
                    glassState = glassState,
                    onCreateRoutine = onManageRoutines
                )
            }
        } else {
            items(shownRoutines, key = { "routine-${it.id}" }) { routine ->
                RoutineSummaryCard(
                    routine = routine,
                    glassState = glassState,
                    onClick = { onStartWorkout(routine.id) },
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = apexTween(motionEnabled, Motion.Fade),
                            placementSpec = apexSpring(motionEnabled),
                            fadeOutSpec = apexTween(motionEnabled, Motion.Micro)
                        )
                        .staggeredEntrance(
                            index = 5 + shownRoutines.indexOf(routine),
                            key = "home-routine-${routine.id}"
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    dayName: String,
    firstName: String,
    initial: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${greeting.uppercase()}  ·  ${dayName.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText
            )
            Spacer(modifier = Modifier.height(Dimens.Space1))
            Text(
                text = "Hey $firstName, ready?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (initial.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.apex.accentText
                )
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHaptics()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText
            )
        }
        TextButton(
            onClick = {
                haptics.tick()
                onAction()
            },
            modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.apex.accentText
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeSkeleton(bottomContentPadding: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = Dimens.ScreenEdge,
                end = Dimens.ScreenEdge,
                top = Dimens.Space3,
                bottom = bottomContentPadding
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                SkeletonBlock(modifier = Modifier.width(120.dp).height(12.dp))
                Spacer(modifier = Modifier.height(Dimens.Space1))
                SkeletonBlock(modifier = Modifier.width(200.dp).height(30.dp))
            }
            SkeletonCircle(size = Dimens.MinTouchTarget)
        }
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(196.dp), shape = CardShape)
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(96.dp), shape = CardShape)
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(88.dp), shape = CardShape)
        Spacer(modifier = Modifier.height(Dimens.Space1))
        SkeletonBlock(modifier = Modifier.width(140.dp).height(22.dp))
        repeat(2) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(80.dp), shape = CardShape)
        }
    }
}

private fun greetingForNow(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

fun fullDayNameFromCode(code: String): String = when (code) {
    "Mon" -> "Monday"
    "Tue" -> "Tuesday"
    "Wed" -> "Wednesday"
    "Thu" -> "Thursday"
    "Fri" -> "Friday"
    "Sat" -> "Saturday"
    "Sun" -> "Sunday"
    else -> code
}

// Streak, workouts and this week as three big numbers
@Composable
private fun HomeMetricsCard(
    streak: Int,
    totalWorkouts: Int,
    weeklyPercent: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val shownStreak by rememberCountUpInt(streak)
    val shownWorkouts by rememberCountUpInt(totalWorkouts)
    val shownPercent by rememberCountUpInt(weeklyPercent)
    val valueStyle = ApexText.Numeral.copy(fontSize = 32.sp, lineHeight = 36.sp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(vertical = Dimens.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricBlock(value = "$shownStreak", label = "STREAK", modifier = Modifier.weight(1f), valueStyle = valueStyle)
        MetricDivider()
        MetricBlock(value = "$shownWorkouts", label = "WORKOUTS", modifier = Modifier.weight(1f), valueStyle = valueStyle)
        MetricDivider()
        MetricBlock(value = "$shownPercent%", label = "THIS WEEK", modifier = Modifier.weight(1f), valueStyle = valueStyle)
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .width(Dimens.Hairline)
            .height(40.dp)
            .background(MaterialTheme.apex.hairline)
    )
}

// Small water card for Home with a +250ml quick add
@Composable
fun WaterWidgetCard(
    todayMl: Int,
    goalMl: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onQuickAdd: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (goalMl <= 0) 0f else (todayMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
    val shownMl by rememberCountUpInt(todayMl)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "WATER",
                style = MaterialTheme.typography.labelMedium,
                color = glassMutedContentColor()
            )
            Spacer(modifier = Modifier.height(Dimens.Space1))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$shownMl",
                    style = ApexText.NumeralSmall,
                    color = glassContentColor()
                )
                Text(
                    text = " / $goalMl ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassMutedContentColor(),
                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.Space2 - 4.dp))
            ApexProgressBar(progress = progressFraction, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onQuickAdd)
                .clip(CircleShape)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add 250ml",
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Today's workout with the main button
@Composable
fun TodaysChallengeCard(routine: Routine, glassState: com.example.apexfitness.ui.theme.GlassState, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val sharedKey = "today-${routine.id}"
    val exerciseCount by rememberCountUpInt(routine.exercises.size)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sharedCardBounds(sharedKey)
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = RoutineIcons.iconFor(routine.iconKey),
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text(
                text = "TODAY'S WORKOUT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.accentText
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space2))

        Text(
            text = routine.name,
            style = MaterialTheme.typography.headlineLarge,
            color = glassContentColor(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(Dimens.Space2))

        MetricBlock(
            value = "$exerciseCount",
            label = if (routine.exercises.size == 1) "EXERCISE" else "EXERCISES",
            valueStyle = ApexText.HeroNumeral,
            valueColor = glassContentColor(),
            horizontalAlignment = Alignment.Start
        )

        Spacer(modifier = Modifier.height(Dimens.Space3))

        ApexPrimaryButton(
            text = "Begin Workout",
            onClick = {
                SharedKeys.lastRoutine = sharedKey
                onStart()
            },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.PlayArrow
        )
    }
}

@Composable
fun NoRoutineTodayCard(modifier: Modifier = Modifier, glassState: com.example.apexfitness.ui.theme.GlassState, onCreateRoutine: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "NOTHING SCHEDULED",
            style = MaterialTheme.typography.labelMedium,
            color = glassMutedContentColor()
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "A quiet day",
            style = MaterialTheme.typography.headlineSmall,
            color = glassContentColor()
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Build a routine and assign it to today so it shows up here automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = glassMutedContentColor()
        )
        Spacer(modifier = Modifier.height(Dimens.Space3))
        ApexPrimaryButton(
            text = "Build a Routine",
            onClick = onCreateRoutine,
            icon = Icons.Outlined.Add
        )
    }
}

@Composable
fun NoRoutinesYetCard(modifier: Modifier = Modifier, glassState: com.example.apexfitness.ui.theme.GlassState, onCreateRoutine: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "No routines yet", style = MaterialTheme.typography.titleMedium, color = glassContentColor())
            Text(text = "Tap to create your first one", style = MaterialTheme.typography.bodyMedium, color = glassMutedContentColor())
        }
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onCreateRoutine)
                .clip(CircleShape)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Create routine",
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun RoutineSummaryCard(
    routine: Routine,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedKey = "list-${routine.id}"
    val schedule = if (routine.days.isEmpty()) "Unscheduled" else routine.days.joinToString(" / ")
    val exerciseCount = routine.exercises.size
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sharedCardBounds(sharedKey)
            .apexClickable(onClick = {
                SharedKeys.lastRoutine = sharedKey
                onClick()
            })
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .clip(CircleShape)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = RoutineIcons.iconFor(routine.iconKey),
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routine.name,
                style = MaterialTheme.typography.titleMedium,
                color = glassContentColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$exerciseCount exercise${if (exerciseCount == 1) "" else "s"}  ·  $schedule",
                style = MaterialTheme.typography.bodySmall,
                color = glassMutedContentColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = glassMutedContentColor()
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "MainScreen Preview"
)
@Composable
fun MainScreenSafePreview() {
    ApexFitnessTheme {
        MainScreen(navController = null)
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Home content - light")
@Composable
private fun HomeContentPreview() {
    ApexFitnessTheme(darkTheme = false) {
        val glassState = rememberGlassState()
        val sample = listOf(
            Routine(id = "a", name = "Upper Body Strength", iconKey = "dumbbell", days = listOf(todayDayCode()), exercises = List(6) { com.example.apexfitness.data.RoutineExercise(name = "Exercise ${it + 1}") }),
            Routine(id = "b", name = "Leg Day", iconKey = "dumbbell", days = listOf("Fri"), exercises = List(5) { com.example.apexfitness.data.RoutineExercise(name = "Exercise ${it + 1}") })
        )
        Box(modifier = Modifier.fillMaxSize().glassScreenBackground(glassState)) {
            HomePage(
                profile = UserProfile(name = "Alex"),
                routines = sample,
                logs = emptyList(),
                todayWaterMl = 1250,
                onStartWorkout = {},
                onManageRoutines = {},
                onSeeAllRoutines = {},
                glassState = glassState
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Home content - dark")
@Composable
private fun HomeContentDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        val glassState = rememberGlassState()
        val sample = listOf(
            Routine(id = "a", name = "Upper Body Strength", iconKey = "dumbbell", days = listOf(todayDayCode()), exercises = List(6) { com.example.apexfitness.data.RoutineExercise(name = "Exercise ${it + 1}") })
        )
        Box(modifier = Modifier.fillMaxSize().glassScreenBackground(glassState)) {
            HomePage(
                profile = UserProfile(name = "Alex"),
                routines = sample,
                logs = emptyList(),
                todayWaterMl = 600,
                onStartWorkout = {},
                onManageRoutines = {},
                onSeeAllRoutines = {},
                glassState = glassState
            )
        }
    }
}
