@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.apexfitness.ui.home

import com.example.apexfitness.ui.settings.UnitPreferences
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.apexfitness.ui.theme.StatusBarScrim
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.apexfitness.ui.theme.ApexSecondaryButton
import com.example.apexfitness.ui.theme.motionSpring
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
import com.example.apexfitness.ui.Workout.WorkoutSummaryScreen
import com.example.apexfitness.ui.body.BodyTrackingScreen
import com.example.apexfitness.ui.body.ProgressPhotosScreen
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
    companion object {
        // Shortcuts and the home screen widget send the screen to open in this extra
        const val EXTRA_DESTINATION = "apex_destination"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Android 15+ forces edge-to-edge. The main tabs draw under the status bar, the other screens get padded (see Pushed)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        // Keep the splash screen up for a moment so the logo can be seen
        val splashStartTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            System.currentTimeMillis() - splashStartTime < 600L
        }
        ThemePreferences.init(applicationContext)
        NotificationPreferences.init(applicationContext)
        UnitPreferences.init(applicationContext)
        NotificationScheduler.createChannel(applicationContext)
        // Only on a fresh start, so rotating the phone does not open the screen again
        val launchDestination = if (savedInstanceState == null) intent?.getStringExtra(EXTRA_DESTINATION) else null
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

                // A shortcut or widget button can open a screen straight away, for a signed in user only
                LaunchedEffect(Unit) {
                    if (launchDestination != null && launchDestination != "main" && startDestination == "main") {
                        runCatching { navController.navigate(launchDestination) }
                    }
                }

                // Screen transitions: fade plus a small slide. Everything is instant if system animations are off.
                val motionEnabled = LocalMotionEnabled.current
                val slidePx = with(LocalDensity.current) { Motion.ScreenSlide.roundToPx() }

                // Solid background behind the NavHost so a fading screen never shows the window background
                SharedTransitionLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
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
                                Pushed {
                                    WelcomeScreen(navController = navController)
                                }
                            }
                            composable("signup") {
                                Pushed {
                                    SignUpScreen(navController = navController, formState = onboardingFormState)
                                }
                            }
                            composable("getStarted") {
                                Pushed {
                                    GetStartedScreen(navController = navController, formState = onboardingFormState)
                                }
                            }
                            composable("fitnessGoals") {
                                Pushed {
                                    FitnessGoalsScreen(navController = navController, formState = onboardingFormState)
                                }
                            }
                            composable("workoutSchedule") {
                                Pushed {
                                    WorkoutScheduleScreen(navController = navController, formState = onboardingFormState)
                                }
                            }
                            composable("signin") {
                                Pushed {
                                    SignInScreen(navController = navController)
                                }
                            }
                            composable("main") {
                                CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                    MainScreen(navController = navController)
                                }
                            }
                            composable("routines") {
                                Pushed {
                                    CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                        RoutineListScreen(navController = navController)
                                    }
                                }
                            }
                            composable("routineEditor/{routineId}") { backStackEntry ->
                                val routineId = backStackEntry.arguments?.getString("routineId") ?: "new"
                                Pushed {
                                    CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                        CreateEditRoutineScreen(navController = navController, routineId = routineId)
                                    }
                                }
                            }
                            composable("workoutSession/{routineId}") { backStackEntry ->
                                val routineId = backStackEntry.arguments?.getString("routineId") ?: return@composable
                                Pushed {
                                    CompositionLocalProvider(LocalNavAnimatedScope provides this@composable) {
                                        WorkoutSessionScreen(navController = navController, routineId = routineId)
                                    }
                                }
                            }
                            composable("editProfile") {
                                Pushed {
                                    EditProfileScreen(navController = navController)
                                }
                            }
                            composable("settings") {
                                Pushed {
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
                            }
                            composable("activityHistory") {
                                Pushed {
                                    ActivityHistoryScreen(navController = navController)
                                }
                            }
                            composable("healthData") {
                                Pushed {
                                    HealthDataScreen(navController = navController)
                                }
                            }
                            composable("achievements") {
                                Pushed {
                                    AchievementsScreen(navController = navController)
                                }
                            }
                            composable("calendar") {
                                Pushed {
                                    CalendarScreen(navController = navController)
                                }
                            }
                            composable("challenges") {
                                Pushed {
                                    ChallengesScreen(navController = navController)
                                }
                            }
                            composable("waterTracking") {
                                Pushed {
                                    WaterTrackingScreen(navController = navController)
                                }
                            }
                            composable("cardio") {
                                Pushed {
                                    CardioTrackingScreen(navController = navController)
                                }
                            }
                            composable("workoutSummary") {
                                Pushed {
                                    WorkoutSummaryScreen(navController = navController)
                                }
                            }
                            composable("bodyProgress") {
                                Pushed {
                                    BodyTrackingScreen(navController = navController)
                                }
                            }
                            composable("progressPhotos") {
                                Pushed {
                                    ProgressPhotosScreen(navController = navController)
                                }
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

fun screenForRoute(route: String): Screen = when (route) {
    Screen.Stats.route -> Screen.Stats
    Screen.Workout.route -> Screen.Workout
    Screen.Profile.route -> Screen.Profile
    else -> Screen.Home
}

// Every screen except the main tabs sits inside the safe area: status bar, navigation bar, notch and keyboard.
// The main tabs handle their own insets so they can scroll under the bars.
@Composable
private fun Pushed(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid

    // The data lives in a ViewModel so it survives trips to other screens
    val mainViewModel: MainViewModel = viewModel()
    // Started straight away (not in an effect) so the very first frame already shows the skeleton.
    // start() does nothing if the listeners are already running for this user, so calling it every time is fine.
    if (uid != null) mainViewModel.start(uid)
    // Coming back to the app after midnight switches the water card to the new day
    LifecycleResumeEffect(Unit) {
        mainViewModel.refreshWaterIfNewDay()
        onPauseOrDispose { }
    }
    val profile = mainViewModel.profile
    val routines = mainViewModel.routines
    val logs = mainViewModel.logs
    val personalRecords = mainViewModel.personalRecords
    val todayWaterMl = mainViewModel.todayWaterMl
    val isInitialLoading = mainViewModel.isInitialLoading

    // Saved so Back from Settings lands on the tab I left, not Home
    var selectedRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }
    val selectedScreen = screenForRoute(selectedRoute)

    // One scroll position per tab, kept when switching tabs or coming back
    val homeListState = rememberLazyListState()
    val statsListState = rememberLazyListState()
    val workoutListState = rememberLazyListState()
    val profileListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    val acknowledgedAchievementIds = remember(profile, mainViewModel.dismissedAchievements) {
        (profile?.acknowledgedAchievementIds ?: emptyList()).toSet() + mainViewModel.dismissedAchievements
    }
    val activeAchievementCelebration = remember(logs.size, acknowledgedAchievementIds, isInitialLoading) {
        if (isInitialLoading) null
        else AchievementTiers
            .filter { logs.size >= it.threshold && it.id !in acknowledgedAchievementIds }
            .minByOrNull { it.threshold }
    }

    fun dismissAchievementCelebration() {
        val tier = activeAchievementCelebration ?: return
        mainViewModel.dismissedAchievements = mainViewModel.dismissedAchievements + tier.id
        val currentUid = uid ?: return
        val updatedAcknowledged = ((profile?.acknowledgedAchievementIds ?: emptyList()) + tier.id).distinct()
        coroutineScope.launch {
            runCatching { FirestoreRepository.updateProfileFields(currentUid, mapOf("acknowledgedAchievementIds" to updatedAcknowledged)) }
        }
    }

    val levelProgress = remember(logs) { GamificationCalculations.levelProgress(logs) }
    val acknowledgedLevel = remember(profile, mainViewModel.dismissedLevelUp) {
        maxOf(profile?.acknowledgedLevel ?: 1, mainViewModel.dismissedLevelUp)
    }
    // Only shown when there is no achievement popup, so they never stack
    val activeLevelUpCelebration = remember(levelProgress.level, acknowledgedLevel, isInitialLoading, activeAchievementCelebration) {
        if (isInitialLoading || activeAchievementCelebration != null) null
        else if (levelProgress.level > acknowledgedLevel) levelProgress.level else null
    }

    fun dismissLevelUpCelebration() {
        val newLevel = activeLevelUpCelebration ?: return
        mainViewModel.dismissedLevelUp = newLevel
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
                onScreenSelected = { screen -> selectedRoute = screen.route },
                glassState = glassState
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
    ) { paddingValues ->
        // No top or bottom padding here on purpose. Each page adds topContentPadding and bottomContentPadding
        // to its list, so content scrolls under the status bar and the tab bar instead of stopping at them.
        val layoutDirection = LocalLayoutDirection.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassScreenBackground(glassState)
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection)
                )
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
                        onSeeAllRoutines = { selectedRoute = Screen.Workout.route },
                        onQuickAddWater = { amount ->
                            uid?.let { id ->
                                coroutineScope.launch { runCatching { FirestoreRepository.addWater(id, amount) } }
                            }
                        },
                        onOpenWaterTracking = { navController?.navigate("waterTracking") },
                        onOpenProfile = { selectedRoute = Screen.Profile.route },
                        isLoading = isInitialLoading,
                        glassState = glassState,
                        topContentPadding = paddingValues.calculateTopPadding(),
                        bottomContentPadding = paddingValues.calculateBottomPadding(),
                        listState = homeListState
                    )
                    is Screen.Stats -> StatsPage(
                        logs = logs,
                        personalRecords = personalRecords,
                        profile = profile,
                        isLoading = isInitialLoading,
                        onOpenCalendar = { navController?.navigate("calendar") },
                        glassState = glassState,
                        topContentPadding = paddingValues.calculateTopPadding(),
                        bottomContentPadding = paddingValues.calculateBottomPadding(),
                        listState = statsListState
                    )
                    is Screen.Workout -> WorkoutTabPage(
                        routines = routines,
                        onStart = ::startWorkout,
                        onManageRoutines = ::goToRoutines,
                        isLoading = isInitialLoading,
                        glassState = glassState,
                        topContentPadding = paddingValues.calculateTopPadding(),
                        bottomContentPadding = paddingValues.calculateBottomPadding(),
                        listState = workoutListState
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
                        onOpenBody = { navController?.navigate("bodyProgress") },
                        onOpenPhotos = { navController?.navigate("progressPhotos") },
                        onShareApp = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "I'm training with ApexFitness - check it out!")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share ApexFitness"))
                        },
                        isLoading = isInitialLoading,
                        glassState = glassState,
                        topContentPadding = paddingValues.calculateTopPadding(),
                        bottomContentPadding = paddingValues.calculateBottomPadding(),
                        listState = profileListState
                    )
                }
            }

            // Soft fade under the status bar so scrolled content does not clash with the clock and icons
            StatusBarScrim()

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

    // Simple tab bar: solid background, one thin line on top, active tab is accent
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

// Home: today's workout is the hero, then a few numbers and water sitting right on the background,
// then your routines in one grouped card. Shows a skeleton while loading.
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
    onOpenProfile: () -> Unit = {},
    isLoading: Boolean = false,
    glassState: com.example.apexfitness.ui.theme.GlassState = rememberGlassState(),
    topContentPadding: androidx.compose.ui.unit.Dp = 0.dp,
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp,
    listState: LazyListState = rememberLazyListState()
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = motionTween(Motion.Standard),
        label = "homeLoadingCrossfade"
    ) { loading ->
        if (loading) {
            HomeSkeleton(topContentPadding = topContentPadding, bottomContentPadding = bottomContentPadding)
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
                onOpenProfile = onOpenProfile,
                glassState = glassState,
                topContentPadding = topContentPadding,
                bottomContentPadding = bottomContentPadding,
                listState = listState
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
    onOpenProfile: () -> Unit,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    topContentPadding: androidx.compose.ui.unit.Dp,
    bottomContentPadding: androidx.compose.ui.unit.Dp,
    listState: LazyListState
) {
    val today = remember { todayDayCode() }
    val todaysRoutines = remember(routines) { routines.filter { it.days.contains(today) } }
    val streak = remember(logs) { StatsCalculations.currentStreak(logs) }
    val totalWorkouts = logs.size
    val weeklyProgress = remember(logs, profile) {
        StatsCalculations.weeklyCompletionPercent(logs, profile?.scheduleDays?.size ?: 7)
    }
    // When each routine was last done, for the "last done Wednesday" line
    val lastDoneByRoutine = remember(logs) {
        logs.groupBy { it.routineId }.mapValues { (_, routineLogs) -> routineLogs.maxOf { it.dateMillis } }
    }
    val firstName = profile?.name?.trim()?.substringBefore(" ").takeUnless { it.isNullOrBlank() } ?: "there"
    val initial = profile?.name?.trim()?.take(1)?.uppercase()
    val greeting = remember { greetingForNow() }
    val shownRoutines = routines.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space2 + topContentPadding,
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
                onOpenProfile = onOpenProfile,
                modifier = Modifier.staggeredEntrance(index = 0, key = "home-header")
            )
        }

        // Today's workout, or an empty state. If more than one is scheduled, only the first gets the main button.
        if (todaysRoutines.isEmpty()) {
            item(key = "hero-empty") {
                NoRoutineTodayCard(
                    modifier = Modifier
                        .padding(top = Dimens.Space1)
                        .staggeredEntrance(index = 1, key = "home-hero-empty"),
                    glassState = glassState,
                    onCreateRoutine = onManageRoutines
                )
            }
        } else {
            itemsIndexed(todaysRoutines, key = { _, routine -> "hero-${routine.id}" }) { index, routine ->
                TodaysChallengeCard(
                    routine = routine,
                    lastDoneMillis = lastDoneByRoutine[routine.id],
                    isPrimary = index == 0,
                    glassState = glassState,
                    onStart = { onStartWorkout(routine.id) },
                    modifier = Modifier
                        .padding(top = if (index == 0) Dimens.Space1 else 0.dp)
                        .staggeredEntrance(index = 1 + index, key = "home-hero-${routine.id}")
                )
            }
        }

        item(key = "metrics") {
            HomeMetricsRow(
                streak = streak,
                totalWorkouts = totalWorkouts,
                weeklyPercent = weeklyProgress,
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 2, key = "home-metrics")
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
                title = "My routines",
                actionLabel = "See all",
                onAction = onSeeAllRoutines,
                modifier = Modifier
                    .padding(top = Dimens.Space1)
                    .staggeredEntrance(index = 4, key = "home-routines-header")
            )
        }

        item(key = "routines") {
            if (routines.isEmpty()) {
                NoRoutinesYetCard(
                    modifier = Modifier.staggeredEntrance(index = 5, key = "home-routines-empty"),
                    glassState = glassState,
                    onCreateRoutine = onManageRoutines
                )
            } else {
                // One grouped card with thin dividers instead of a separate card per routine
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(index = 5, key = "home-routines")
                        .glassPanel(glassState, shape = CardShape)
                        .animateContentSize(motionSpring())
                ) {
                    shownRoutines.forEachIndexed { index, routine ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 76.dp)
                                    .fillMaxWidth()
                                    .height(Dimens.Hairline)
                                    .background(MaterialTheme.apex.hairline)
                            )
                        }
                        key(routine.id) {
                            RoutineSummaryCard(
                                routine = routine,
                                glassState = glassState,
                                onClick = { onStartWorkout(routine.id) }
                            )
                        }
                    }
                }
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
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${dayName.uppercase()}  ·  ${greeting.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hey $firstName",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        // The avatar opens the Profile tab
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onOpenProfile)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape)
                .semantics { contentDescription = "Open profile" },
            contentAlignment = Alignment.Center
        ) {
            if (initial.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
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
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
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
                color = MaterialTheme.apex.mutedText
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.apex.mutedText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeSkeleton(topContentPadding: androidx.compose.ui.unit.Dp, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = Dimens.ScreenEdge,
                end = Dimens.ScreenEdge,
                top = Dimens.Space2 + topContentPadding,
                bottom = bottomContentPadding
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBlock(modifier = Modifier.width(160.dp).height(12.dp))
                Spacer(modifier = Modifier.height(Dimens.Space1))
                SkeletonBlock(modifier = Modifier.width(110.dp).height(20.dp))
            }
            SkeletonCircle(size = Dimens.MinTouchTarget)
        }
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(236.dp), shape = RoundedCornerShape(24.dp))
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space3)) {
            repeat(3) { SkeletonBlock(modifier = Modifier.weight(1f).height(56.dp)) }
        }
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(72.dp))
        Spacer(modifier = Modifier.height(Dimens.Space1))
        SkeletonBlock(modifier = Modifier.width(140.dp).height(24.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(152.dp), shape = CardShape)
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

// "today", "yesterday", "Wednesday" within the last week, otherwise "12 Sep"
private fun lastDoneLabel(millis: Long): String {
    val dayMillis = 24L * 60 * 60 * 1000
    val startOfToday = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    val daysAgo = when {
        millis >= startOfToday -> 0L
        else -> (startOfToday - millis) / dayMillis + 1
    }
    return when {
        daysAgo == 0L -> "today"
        daysAgo == 1L -> "yesterday"
        daysAgo < 7L -> java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(java.util.Date(millis))
        else -> java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault()).format(java.util.Date(millis))
    }
}

// 1250 becomes "1,250"
private fun groupThousands(value: Int): String = "%,d".format(value)

// Streak, workouts and this week as three big numbers, sitting on the background with thin dividers
@Composable
private fun HomeMetricsRow(
    streak: Int,
    totalWorkouts: Int,
    weeklyPercent: Int,
    modifier: Modifier = Modifier
) {
    val shownStreak by rememberCountUpInt(streak)
    val shownWorkouts by rememberCountUpInt(totalWorkouts)
    val shownPercent by rememberCountUpInt(weeklyPercent)
    val valueStyle = ApexText.Numeral.copy(fontSize = 40.sp, lineHeight = 42.sp)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricBlock(value = "$shownStreak", label = "DAY STREAK", modifier = Modifier.weight(1f), valueStyle = valueStyle, valueColor = MaterialTheme.colorScheme.onBackground)
        MetricDivider()
        MetricBlock(value = "$shownWorkouts", label = "WORKOUTS", modifier = Modifier.weight(1f), valueStyle = valueStyle, valueColor = MaterialTheme.colorScheme.onBackground)
        MetricDivider()
        MetricBlock(value = "$shownPercent%", label = "THIS WEEK", modifier = Modifier.weight(1f), valueStyle = valueStyle, valueColor = MaterialTheme.colorScheme.onBackground)
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

// Water for today as a row between two hairlines, with a +250ml quick add
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
    val hairline = MaterialTheme.apex.hairline
    Row(
        modifier = modifier
            .fillMaxWidth()
            .apexClickable(pressedScale = 0.99f, onClick = onClick)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(hairline, Offset(0f, 0f), Offset(size.width, 0f), stroke)
                drawLine(hairline, Offset(0f, size.height), Offset(size.width, size.height), stroke)
            }
            .padding(vertical = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "WATER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = groupThousands(shownMl),
                    style = ApexText.NumeralSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = " / ${groupThousands(goalMl)} ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText,
                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.Space1 + 2.dp))
            ApexProgressBar(progress = progressFraction, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.width(Dimens.Space3))
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onQuickAdd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(Dimens.Hairline, hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add 250 ml of water",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Today's workout: the hero of Home. The routine name is big, the details are one quiet line.
@Composable
fun TodaysChallengeCard(
    routine: Routine,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    lastDoneMillis: Long? = null,
    isPrimary: Boolean = true
) {
    val sharedKey = "today-${routine.id}"
    val exerciseCount = routine.exercises.size
    val setCount = routine.exercises.sumOf { it.sets.coerceAtLeast(1) }
    val details = buildList {
        add("$exerciseCount exercise${if (exerciseCount == 1) "" else "s"}")
        if (setCount > 0) add("$setCount sets")
        lastDoneMillis?.let { add("last done ${lastDoneLabel(it)}") }
    }.joinToString("  ·  ")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sharedCardBounds(sharedKey)
            .glassPanel(glassState, shape = RoundedCornerShape(24.dp))
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "TODAY'S WORKOUT",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.accentText
        )
        Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))
        Text(
            text = routine.name,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 42.sp, lineHeight = 44.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))
        Text(
            text = details,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space3))
        val onBegin = {
            SharedKeys.lastRoutine = sharedKey
            onStart()
        }
        if (isPrimary) {
            ApexPrimaryButton(
                text = "Begin workout",
                onClick = onBegin,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.PlayArrow
            )
        } else {
            ApexSecondaryButton(
                text = "Begin workout",
                onClick = onBegin,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.PlayArrow
            )
        }
    }
}

@Composable
fun NoRoutineTodayCard(modifier: Modifier = Modifier, glassState: com.example.apexfitness.ui.theme.GlassState, onCreateRoutine: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = RoundedCornerShape(24.dp))
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "NOTHING SCHEDULED",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))
        Text(
            text = "Rest day",
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 42.sp, lineHeight = 44.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Nothing is set for today. Build a routine and pick its days, and it will show up here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space3))
        ApexSecondaryButton(
            text = "Build a routine",
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
            .apexClickable(onClick = onCreateRoutine)
            .glassPanel(glassState, shape = CardShape)
            .padding(horizontal = Dimens.Space2, vertical = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "No routines yet", style = MaterialTheme.typography.titleMedium, color = glassContentColor())
            Text(text = "Tap to create your first one", style = MaterialTheme.typography.bodyMedium, color = glassMutedContentColor())
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = glassMutedContentColor()
        )
    }
}

// One routine row inside the grouped "My routines" card
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
            .apexClickable(pressedScale = 0.99f, onClick = {
                SharedKeys.lastRoutine = sharedKey
                onClick()
            })
            .heightIn(min = 72.dp)
            .padding(horizontal = Dimens.Space2, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = RoutineIcons.iconFor(routine.iconKey),
                contentDescription = null,
                tint = MaterialTheme.apex.mutedText,
                modifier = Modifier.size(20.dp)
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
