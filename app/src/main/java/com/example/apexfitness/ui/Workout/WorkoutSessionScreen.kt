package com.example.apexfitness.ui.Workout

import com.example.apexfitness.ui.settings.UnitPreferences
import com.example.apexfitness.ui.settings.OnboardingPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.LoggedExercise
import com.example.apexfitness.data.LoggedSet
import com.example.apexfitness.data.Routine
import com.example.apexfitness.data.RoutineExercise
import com.example.apexfitness.ui.routines.ExercisePickerDialog
import com.example.apexfitness.data.StatsCalculations
import com.example.apexfitness.data.WorkoutLog
import com.example.apexfitness.ui.authentication.AuthService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import com.example.apexfitness.data.ExerciseHistory
import com.example.apexfitness.data.ExerciseInfo
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.example.apexfitness.ui.theme.*

// One set row in a live session (what was actually done)
class SetEntry(reps: String, weight: String) {
    var reps by mutableStateOf(reps)
    var weight by mutableStateOf(weight)
    var completed by mutableStateOf(false)
}

class ExerciseSession(exercise: RoutineExercise, val sets: SnapshotStateList<SetEntry>) {
    var exercise by mutableStateOf(exercise)
}

// Rest times offered in the rest timer, in seconds
private val RestTimerPresets = listOf(30, 60, 90, 120)

// If previewRoutine is set it is used instead of loading from Firestore, so the preview works without a user.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(navController: NavHostController, routineId: String, previewRoutine: Routine? = null) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()
    val useLbs = UnitPreferences.useLbs.collectAsState().value

    var routine by remember { mutableStateOf<Routine?>(previewRoutine) }
    var isLoading by remember { mutableStateOf(previewRoutine == null) }
    var isSaving by remember { mutableStateOf(false) }
    var restSecondsLeft by remember { mutableStateOf(0) }
    var restTotalSeconds by remember { mutableStateOf(0) }
    var isRestPaused by remember { mutableStateOf(false) }
    var restTimerJob by remember { mutableStateOf<Job?>(null) }
    var restPromptSeconds by remember { mutableStateOf<Int?>(null) }
    var showGlossary by remember { mutableStateOf(!OnboardingPreferences.hasSeenWorkoutGlossary(context)) }
    var favoriteExerciseNames by remember { mutableStateOf<Set<String>>(emptySet()) }
    var swapTargetIndex by remember { mutableStateOf<Int?>(null) }
    val startTimeMillis = remember { System.currentTimeMillis() }
    // What the user did last time and their best, by exercise name (lowercase)
    var history by remember { mutableStateOf<Map<String, ExerciseHistory>>(emptyMap()) }

    val sessionExercises = remember { mutableStateListOf<ExerciseSession>() }

    val glassState = rememberGlassState()
    val haptics = rememberHaptics()

    LaunchedEffect(routineId, uid) {
        val loaded = previewRoutine ?: if (uid != null) FirestoreRepository.getRoutine(uid, routineId) else null
        run {
            routine = loaded
            sessionExercises.clear()
            loaded?.exercises?.sortedBy { it.order }?.forEach { ex ->
                val sets = mutableStateListOf<SetEntry>()
                val prefillReps = ex.reps.takeIf { it.all { c -> c.isDigit() } } ?: ""
                repeat(ex.sets.coerceAtLeast(1)) {
                    sets.add(SetEntry(reps = prefillReps, weight = ex.targetWeight))
                }
                sessionExercises.add(ExerciseSession(ex, sets))
            }
        }
        isLoading = false
    }

    LaunchedEffect(uid) {
        val id = uid ?: return@LaunchedEffect
        runCatching {
            val records = FirestoreRepository.observePersonalRecords(id).first()
            // Newest workouts come first, so the first match for an exercise is the latest one
            val logs = FirestoreRepository.observeWorkoutLogs(id, 30).first()
            val map = mutableMapOf<String, ExerciseHistory>()
            records.forEach { record ->
                map[record.exerciseName.trim().lowercase()] = ExerciseHistory(best = record)
            }
            logs.forEach { log ->
                log.exercises.forEach { exercise ->
                    val key = exercise.name.trim().lowercase()
                    val existing = map[key]
                    if (existing?.lastSet == null) {
                        val bestSet = exercise.sets.filter { it.completed }.maxByOrNull { it.weight * 1000 + it.reps }
                        if (bestSet != null) {
                            map[key] = ExerciseHistory(lastSet = bestSet, best = existing?.best)
                        }
                    }
                }
            }
            map
        }.onSuccess { history = it }
    }

    LaunchedEffect(uid) {
        val id = uid ?: return@LaunchedEffect
        runCatching { FirestoreRepository.getProfile(id)?.favoriteExerciseNames?.toSet() ?: emptySet() }
            .onSuccess { favoriteExerciseNames = it }
    }

    // Lets someone swap in an exercise from the library without leaving the workout,
    // e.g. when a machine is taken. Keeps the same target sets/reps/rest, just a new name.
    fun toggleFavoriteExercise(exerciseName: String) {
        val updated = if (favoriteExerciseNames.contains(exerciseName)) {
            favoriteExerciseNames - exerciseName
        } else {
            favoriteExerciseNames + exerciseName
        }
        favoriteExerciseNames = updated
        val currentUid = uid
        if (currentUid != null) {
            coroutineScope.launch {
                runCatching { FirestoreRepository.updateProfileFields(currentUid, mapOf("favoriteExerciseNames" to updated.toList())) }
            }
        }
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        if (seconds <= 0) return
        restTotalSeconds = seconds
        restSecondsLeft = seconds
        isRestPaused = false
        restTimerJob = coroutineScope.launch {
            while (isActive && restSecondsLeft > 0) {
                delay(1000)
                if (!isRestPaused && restSecondsLeft > 0) {
                    restSecondsLeft -= 1
                }
            }
            if (restSecondsLeft <= 0) {
                triggerRestCompleteFeedback(haptics)
            }
        }
    }

    fun pauseResumeRestTimer() {
        if (restTimerJob != null && restSecondsLeft > 0) {
            isRestPaused = !isRestPaused
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        restSecondsLeft = 0
        restTotalSeconds = 0
        isRestPaused = false
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        if (restSecondsLeft <= 0 && restTimerJob == null) return
        val next = (restSecondsLeft + deltaSeconds).coerceAtLeast(0)
        if (next <= 0) {
            skipRestTimer()
        } else {
            restSecondsLeft = next
            if (next > restTotalSeconds) restTotalSeconds = next
        }
    }

    fun finishWorkout() {
        val currentUid = uid ?: return
        val currentRoutine = routine ?: return
        isSaving = true
        val durationMinutes = (((System.currentTimeMillis() - startTimeMillis) / 60000L).toInt()).coerceAtLeast(1)
        val loggedExercises = sessionExercises.map { session ->
            LoggedExercise(
                name = session.exercise.name,
                sets = session.sets.mapIndexed { index, set ->
                    LoggedSet(
                        setNumber = index + 1,
                        reps = set.reps.toIntOrNull() ?: 0,
                        weight = UnitPreferences.toKg(set.weight.toDoubleOrNull() ?: 0.0, useLbs),  // saved in kg
                        completed = set.completed
                    )
                }
            )
        }
        val completedSets = loggedExercises.sumOf { ex -> ex.sets.count { it.completed } }

        WorkoutSummaryHolder.current = null
        coroutineScope.launch {
            runCatching {
                val profile = FirestoreRepository.getProfile(currentUid)
                val estimatedCalories = StatsCalculations.estimateCaloriesBurned(durationMinutes, completedSets, profile?.weightKg)
                FirestoreRepository.addWorkoutLog(
                    currentUid,
                    WorkoutLog(
                        routineId = currentRoutine.id,
                        routineName = currentRoutine.name,
                        durationMinutes = durationMinutes,
                        caloriesBurned = estimatedCalories,
                        exercises = loggedExercises
                    )
                )
                val results = mutableListOf<ExerciseResult>()
                loggedExercises.forEach { ex ->
                    val bestSet = ex.sets.filter { it.completed }.maxByOrNull { it.weight * 1000 + it.reps }
                    if (bestSet != null && (bestSet.weight > 0 || bestSet.reps > 0)) {
                        val isRecord = FirestoreRepository.upsertPersonalRecordIfBetter(currentUid, ex.name, bestSet.weight, bestSet.reps)
                        results.add(ExerciseResult(ex.name, bestSet.weight, bestSet.reps, isRecord))
                    }
                }
                // Hand the finished workout to the summary screen
                WorkoutSummaryHolder.current = WorkoutSummary(
                    routineName = currentRoutine.name,
                    dateMillis = System.currentTimeMillis(),
                    durationMinutes = durationMinutes,
                    completedSets = completedSets,
                    totalSets = loggedExercises.sumOf { it.sets.size },
                    volumeKg = loggedExercises.sumOf { ex -> ex.sets.filter { it.completed }.sumOf { it.weight * it.reps } },
                    calories = estimatedCalories,
                    exercises = results
                )
            }
            isSaving = false
            if (WorkoutSummaryHolder.current != null) {
                // Replace the session with the summary so Back goes to the home screen
                navController.navigate("workoutSummary") {
                    popUpTo("workoutSession/{routineId}") { inclusive = true }
                }
            } else {
                navController.popBackStack()
            }
        }
    }

    // Loading, not found and content fade into each other
    val uiState = when {
        isLoading -> 0
        routine == null -> 1
        else -> 2
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sharedCardBounds(SharedKeys.lastRoutine)
            .glassScreenBackground(glassState)
    ) {
        Crossfade(
            targetState = uiState,
            animationSpec = motionTween(Motion.Standard),
            label = "sessionState"
        ) { state ->
            when (state) {
                0 -> SessionSkeleton()
                1 -> RoutineNotFound(onBack = { navController.popBackStack() })
                else -> {
                    val currentRoutine = routine
                    if (currentRoutine != null) {
                        SessionBody(
                            routineName = currentRoutine.name,
                            startTimeMillis = startTimeMillis,
                            sessionExercises = sessionExercises,
                            restSecondsLeft = restSecondsLeft,
                            restTotalSeconds = restTotalSeconds,
                            isRestPaused = isRestPaused,
                            isSaving = isSaving,
                            glassState = glassState,
                            history = history,
                            onClose = { navController.popBackStack() },
                            onFinish = { finishWorkout() },
                            onSetCompleted = { session -> restPromptSeconds = session.exercise.restSeconds },
                            onSwapRequested = { index -> swapTargetIndex = index },
                            onPauseResume = { pauseResumeRestTimer() },
                            onSkip = { skipRestTimer() },
                            onAdjust = { delta -> adjustRestTimer(delta) },
                            onPreset = { seconds -> startRestTimer(seconds) },
                            restPromptSeconds = restPromptSeconds,
                            onStartRestPrompt = { seconds -> restPromptSeconds = null; startRestTimer(seconds) },
                            onDismissRestPrompt = { restPromptSeconds = null },
                            showGlossary = showGlossary,
                            onDismissGlossary = {
                                showGlossary = false
                                OnboardingPreferences.setSeenWorkoutGlossary(context)
                            }
                        )
                    }
                }
            }
        }

        if (swapTargetIndex != null) {
            ExercisePickerDialog(
                favoriteExerciseNames = favoriteExerciseNames,
                onToggleFavorite = ::toggleFavoriteExercise,
                onDismiss = { swapTargetIndex = null },
                onPick = { name ->
                    val idx = swapTargetIndex
                    if (idx != null && idx in sessionExercises.indices) {
                        sessionExercises[idx].exercise = sessionExercises[idx].exercise.copy(name = name)
                    }
                    swapTargetIndex = null
                }
            )
        }
    }
}

// What sits in the bar at the bottom of the workout
private enum class SessionBottomMode { Rest, Prompt, Finish }

@Composable
private fun SessionBody(
    routineName: String,
    startTimeMillis: Long,
    sessionExercises: List<ExerciseSession>,
    restSecondsLeft: Int,
    restTotalSeconds: Int,
    isRestPaused: Boolean,
    isSaving: Boolean,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    history: Map<String, ExerciseHistory> = emptyMap(),
    onClose: () -> Unit,
    onFinish: () -> Unit,
    onSetCompleted: (ExerciseSession) -> Unit,
    onSwapRequested: (Int) -> Unit = {},
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    onAdjust: (Int) -> Unit,
    onPreset: (Int) -> Unit,
    restPromptSeconds: Int? = null,
    onStartRestPrompt: (Int) -> Unit = {},
    onDismissRestPrompt: () -> Unit = {},
    showGlossary: Boolean = false,
    onDismissGlossary: () -> Unit = {}
) {
    val motionEnabled = LocalMotionEnabled.current
    val totalSets = sessionExercises.sumOf { it.sets.size }
    val doneSets = sessionExercises.sumOf { exercise -> exercise.sets.count { it.completed } }
    val sessionProgress = if (totalSets == 0) 0f else doneSets.toFloat() / totalSets.toFloat()
    val totalExercises = sessionExercises.size
    // The exercise with the next set to do. -1 once everything is checked off.
    val currentExerciseIndex = sessionExercises.indexOfFirst { session -> session.sets.any { !it.completed } }
    val currentExerciseNumber = if (currentExerciseIndex >= 0) currentExerciseIndex + 1 else totalExercises
    val allDone = totalSets > 0 && doneSets == totalSets
    val statusLabel = when {
        totalExercises == 0 -> "NO EXERCISES"
        allDone -> "ALL SETS DONE"
        else -> "EXERCISE $currentExerciseNumber OF $totalExercises"
    }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var showFinishConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    // Jumps to whichever exercise is next, so finishing one doesn't leave you hunting
    // for the next one in a long routine.
    LaunchedEffect(currentExerciseIndex, showGlossary) {
        if (currentExerciseIndex >= 0) {
            val itemIndex = currentExerciseIndex + if (showGlossary) 1 else 0
            listState.animateScrollToItem(itemIndex)
        }
    }

    fun requestClose() {
        if (doneSets > 0) showDiscardConfirm = true else onClose()
    }

    // The back gesture asks too, same as the close button, so a swipe can't throw a workout away
    BackHandler(enabled = doneSets > 0 && !isSaving) { showDiscardConfirm = true }

    // Keep the screen on during a workout so the phone does not lock between sets
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val bottomMode = when {
        restSecondsLeft > 0 -> SessionBottomMode.Rest
        restPromptSeconds != null -> SessionBottomMode.Prompt
        else -> SessionBottomMode.Finish
    }
    // The list gets extra space at the bottom so the last set is never stuck under the bar
    val density = LocalDensity.current
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val background = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SessionHeader(
                routineName = routineName,
                statusLabel = statusLabel,
                onClose = { requestClose() }
            )
            SessionClock(
                startTimeMillis = startTimeMillis,
                doneSets = doneSets,
                totalSets = totalSets,
                progress = sessionProgress
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(
                    start = Dimens.ScreenEdge,
                    top = Dimens.Space2,
                    end = Dimens.ScreenEdge,
                    bottom = bottomBarHeight + Dimens.Space3
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
            ) {
                if (showGlossary) {
                    item(key = "glossary") {
                        WorkoutGlossaryCard(
                            glassState = glassState,
                            onDismiss = onDismissGlossary,
                            modifier = Modifier.padding(bottom = Dimens.Space1)
                        )
                    }
                }
                items(sessionExercises.size, key = { index -> "exercise-$index" }) { index ->
                    val session = sessionExercises[index]
                    val isDone = session.sets.isNotEmpty() && session.sets.all { it.completed }
                    val isCurrent = !isDone && index == currentExerciseIndex
                    SessionExerciseCard(
                        session = session,
                        history = history[session.exercise.name.trim().lowercase()],
                        onSetCompleted = { onSetCompleted(session) },
                        onSwap = { onSwapRequested(index) },
                        isCurrent = isCurrent,
                        isDone = isDone,
                        modifier = Modifier.staggeredEntrance(index)
                    )
                }
            }
        }

        // Soft fade so the list slides under the bottom bar instead of being cut off
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomBarHeight + Dimens.Space3)
                .background(Brush.verticalGradient(listOf(background.copy(alpha = 0f), background, background)))
        )

        // One bar at a time: the rest timer, the "rest now?" prompt, or Finish
        AnimatedContent(
            targetState = bottomMode,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { bottomBarHeight = with(density) { it.height.toDp() } }
                .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space2),
            transitionSpec = {
                if (motionEnabled) {
                    (fadeIn(apexTween(true, Motion.Standard)) + slideInVertically(apexSpring(true)) { it / 3 }) togetherWith
                        (fadeOut(apexTween(true, Motion.Micro + 30)) + slideOutVertically(apexSpring(true)) { it / 3 }) using
                        SizeTransform(clip = false)
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "sessionBottomBar"
        ) { mode ->
            when (mode) {
                SessionBottomMode.Rest -> RestDock(
                    secondsLeft = restSecondsLeft,
                    totalSeconds = restTotalSeconds,
                    isPaused = isRestPaused,
                    glassState = glassState,
                    onPauseResume = onPauseResume,
                    onSkip = onSkip,
                    onAdjust = onAdjust,
                    onPreset = onPreset
                )
                SessionBottomMode.Prompt -> RestPromptBar(
                    seconds = restPromptSeconds ?: 0,
                    glassState = glassState,
                    onStart = { onStartRestPrompt(restPromptSeconds ?: 0) },
                    onDismiss = onDismissRestPrompt
                )
                SessionBottomMode.Finish -> FinishBar(
                    allDone = allDone,
                    isSaving = isSaving,
                    onFinish = { if (totalSets > 0 && doneSets < totalSets) showFinishConfirm = true else onFinish() }
                )
            }
        }
    }

    // A stray tap on Close should not silently wipe a workout that is already underway.
    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = ApexShapes.large,
            title = {
                Text(
                    text = "Discard this workout?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "You have logged $doneSets of $totalSets sets. Leaving now throws that away.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showDiscardConfirm = false; onClose() },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Discard", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apex.errorText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardConfirm = false },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Keep training", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Finishing with sets still unchecked is often a mistake made in a hurry, so double check.
    if (showFinishConfirm) {
        AlertDialog(
            onDismissRequest = { showFinishConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = ApexShapes.large,
            title = {
                Text(
                    text = "Finish with sets left?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "You have ${totalSets - doneSets} unchecked sets. You can still finish and save what you did.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showFinishConfirm = false; onFinish() },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Finish anyway", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apex.accentText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFinishConfirm = false },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Keep training", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

// Close button, routine name and where you are in the workout
@Composable
private fun SessionHeader(
    routineName: String,
    statusLabel: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onClose)
                .clip(CircleShape)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close workout",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routineName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
        }
    }
}

// The hero of the screen: how long you have been training, plus sets done and a thin progress line
@Composable
private fun SessionClock(
    startTimeMillis: Long,
    doneSets: Int,
    totalSets: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Ticks once a second on the second. Only this small part redraws, not the whole screen.
    val elapsedSeconds by produceState(initialValue = secondsSince(startTimeMillis), startTimeMillis) {
        while (true) {
            value = secondsSince(startTimeMillis)
            delay(1000L - (System.currentTimeMillis() - startTimeMillis) % 1000L)
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.ScreenEdge, end = Dimens.ScreenEdge, top = Dimens.Space1)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                RollingText(
                    text = formatClock(elapsedSeconds),
                    style = ApexText.HeroNumeral.copy(fontSize = 64.sp, lineHeight = 66.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(text = "ELAPSED", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.apex.mutedText)
            }
            MetricBlock(
                value = "$doneSets/$totalSets",
                label = "SETS",
                valueStyle = ApexText.NumeralSmall,
                horizontalAlignment = Alignment.End,
                valueColor = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        ApexProgressBar(progress = progress, height = 2.dp)
    }
}

private fun secondsSince(startMillis: Long): Int =
    ((System.currentTimeMillis() - startMillis) / 1000L).toInt().coerceAtLeast(0)

// 4:05 under an hour, 1:04:05 after that
private fun formatClock(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}

// Finish is quiet while sets are left and becomes the main button once everything is ticked off
@Composable
private fun FinishBar(
    allDone: Boolean,
    isSaving: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = allDone,
        animationSpec = motionTween(Motion.Standard),
        modifier = modifier.fillMaxWidth(),
        label = "finishBar"
    ) { done ->
        val text = if (isSaving) "Saving" else "Finish workout"
        if (done) {
            ApexPrimaryButton(
                text = text,
                onClick = onFinish,
                enabled = !isSaving,
                icon = Icons.Outlined.Check,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ApexSecondaryButton(
                text = text,
                onClick = onFinish,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SessionSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space1),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonCircle(size = Dimens.MinTouchTarget)
            Spacer(modifier = Modifier.width(Dimens.Space2))
            SkeletonBlock(modifier = Modifier.width(160.dp).height(18.dp))
        }
        SkeletonBlock(modifier = Modifier.width(150.dp).height(60.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(2.dp))
        repeat(2) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(220.dp), shape = CardShape)
        }
    }
}

@Composable
private fun RoutineNotFound(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.Space3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Routine not found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimens.Space3))
        ApexPrimaryButton(text = "Go back", onClick = onBack)
    }
}

private fun formatRestTime(seconds: Int): String {
    if (seconds < 60) return "${seconds}s"
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}

// Soft beep and a small vibration when the rest ends on its own
private fun triggerRestCompleteFeedback(haptics: Haptics) {
    runCatching {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
    }
    haptics.soft()
}

// A calm prompt after a set is completed - rest starts only when the user taps Start.
@Composable
private fun RestPromptBar(
    seconds: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = RoundedCornerShape(28.dp))
            .padding(start = Dimens.Space3, end = Dimens.Space1, top = Dimens.Space1, bottom = Dimens.Space1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SET DONE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
            Text(
                text = "Rest ${formatRestTime(seconds)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
        ) {
            Text(text = "Not now", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apex.mutedText)
        }
        ApexPrimaryButton(text = "Start", onClick = onStart, icon = Icons.Outlined.PlayArrow)
    }
}

// Rest timer docked at the bottom. Small by default (ring, time, pause, skip) so the sets stay visible.
// Tap the time to open the extra controls: +/-15 seconds and the preset lengths.
@Composable
private fun RestDock(
    secondsLeft: Int,
    totalSeconds: Int,
    isPaused: Boolean,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    onAdjust: (Int) -> Unit,
    onPreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val motionEnabled = LocalMotionEnabled.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.apex.mutedText
    val accentColor = MaterialTheme.apex.accent
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val total = totalSeconds.coerceAtLeast(1)
    var expanded by rememberSaveable { mutableStateOf(false) }

    // The ring drains a little every second. This is the only linear animation, because it shows real time passing.
    // A bigger jump (+/-15s or a preset) uses a spring instead.
    val ring = remember { Animatable(secondsLeft.toFloat() / total) }
    LaunchedEffect(secondsLeft, isPaused, totalSeconds, motionEnabled) {
        val now = secondsLeft.toFloat() / total
        if (abs(ring.value - now) > 2f / total) {
            ring.animateTo(now, spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow))
        } else {
            ring.snapTo(now)
        }
        if (!isPaused && motionEnabled && secondsLeft > 0) {
            ring.animateTo((secondsLeft - 1).toFloat() / total, tween(1000, easing = LinearEasing))
        }
    }

    // Small pulse in the last 3 seconds
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(secondsLeft) {
        if (motionEnabled && !isPaused && secondsLeft in 1..3) {
            pulse.animateTo(1.06f, tween(Motion.Micro, easing = FastOutSlowInEasing))
            pulse.animateTo(1f, tween(Motion.Fade, easing = FastOutSlowInEasing))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = RoundedCornerShape(28.dp))
            .animateContentSize(apexSpring(motionEnabled))
            .padding(horizontal = Dimens.Space2, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        scaleX = pulse.value
                        scaleY = pulse.value
                    }
            ) {
                val strokeWidth = 4.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth))
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.value.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.MinTouchTarget)
                    .apexClickable(haptic = false, pressedScale = 1f) { expanded = !expanded }
                    .semantics { contentDescription = if (expanded) "Hide rest options" else "Show rest options" },
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPaused) "REST PAUSED" else "REST",
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedColor
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                        contentDescription = null,
                        tint = mutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                RollingText(
                    text = formatRestTime(secondsLeft),
                    style = ApexText.Numeral.copy(fontSize = 34.sp, lineHeight = 38.sp),
                    color = contentColor
                )
            }
            RestTimerIconButton(
                icon = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                contentDescription = if (isPaused) "Resume rest" else "Pause rest",
                onClick = onPauseResume,
                emphasized = true
            )
            Spacer(modifier = Modifier.width(Dimens.Space1))
            RestTimerIconButton(icon = Icons.Outlined.SkipNext, contentDescription = "Skip rest", onClick = onSkip)
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(apexTween(motionEnabled, Motion.Fade)) + expandVertically(apexSpring(motionEnabled)),
            exit = fadeOut(apexTween(motionEnabled, Motion.Micro)) + shrinkVertically(apexSpring(motionEnabled))
        ) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                    RestChip(text = "-15s", selected = false, onClick = { onAdjust(-15) }, modifier = Modifier.weight(1f))
                    RestChip(text = "+15s", selected = false, onClick = { onAdjust(15) }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                    RestTimerPresets.forEach { presetSeconds ->
                        RestChip(
                            text = formatRestTime(presetSeconds),
                            selected = totalSeconds == presetSeconds,
                            onClick = { onPreset(presetSeconds) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Pill used for the rest presets and the +/-15s buttons. 48dp tall so it is easy to hit.
@Composable
private fun RestChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.apex.accent
    Box(
        modifier = modifier
            .heightIn(min = Dimens.MinTouchTarget)
            .apexClickable(onClick = onClick)
            .clip(PillShape)
            .background(if (selected) MaterialTheme.apex.accentSoft else MaterialTheme.colorScheme.background)
            .border(Dimens.Hairline, if (selected) accent else MaterialTheme.apex.hairline, PillShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.apex.accentText else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RestTimerIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    emphasized: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(Dimens.MinTouchTarget)
            .apexClickable(onClick = onClick)
            .clip(CircleShape)
            .background(if (emphasized) MaterialTheme.apex.accentSoft else Color.Transparent)
            .border(
                Dimens.Hairline,
                if (emphasized) MaterialTheme.apex.accent else MaterialTheme.apex.hairline,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (emphasized) MaterialTheme.apex.accentText else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Shown once, the first time someone opens a workout, to explain the basic terms.
@Composable
private fun WorkoutGlossaryCard(
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = glassContentColor()
    val mutedColor = glassMutedContentColor()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "New to this? Quick guide",
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "A SET is one round of an exercise. REPS is how many times you repeat the " +
                "movement in that round. WEIGHT is how much you're lifting. After each set, " +
                "you can start a short REST before the next one.",
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor
        )
        Spacer(modifier = Modifier.height(Dimens.Space2))
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text("Got it")
        }
    }
}

// Small "NOW" / "DONE" label shown on an exercise card during a workout
@Composable
private fun ExerciseStatusPill(text: String, accent: Boolean) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(if (accent) MaterialTheme.apex.accentSoft else Color.Transparent)
            .then(if (accent) Modifier else Modifier.border(Dimens.Hairline, MaterialTheme.apex.hairline, PillShape))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) MaterialTheme.apex.accentText else MaterialTheme.apex.mutedText
        )
    }
}

@Composable
private fun SessionExerciseCard(
    session: ExerciseSession,
    onSetCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    history: ExerciseHistory? = null,
    isCurrent: Boolean = false,
    isDone: Boolean = false,
    onSwap: () -> Unit = {}
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.apex.mutedText
    val haptics = rememberHaptics()
    val useLbs = UnitPreferences.useLbs.collectAsState().value
    val tip = remember(session.exercise.name) { ExerciseInfo.find(session.exercise.name) }
    // Auto-open the form tip for whichever exercise you're currently on, since that's the one
    // a beginner actually needs guidance for right now. Still collapsible by hand.
    var showTip by remember { mutableStateOf(isCurrent) }
    LaunchedEffect(isCurrent) {
        if (isCurrent) showTip = true
    }

    // "80 kg × 8" for a weighted set, "12 reps" for bodyweight
    fun describe(weightKg: Double, reps: Int): String =
        if (weightKg > 0) {
            "${UnitPreferences.format(UnitPreferences.fromKg(weightKg, useLbs))} ${UnitPreferences.label(useLbs)} × $reps"
        } else {
            "$reps reps"
        }
    val previousParts = buildList {
        history?.lastSet?.let { add("Last ${describe(it.weight, it.reps)}") }
        history?.best?.takeIf { it.bestWeight > 0 || it.bestReps > 0 }?.let { add("Best ${describe(it.bestWeight, it.bestReps)}") }
    }

    // The current exercise gets an accent border instead of the hairline, so it stands out without a second border
    val borderColor = if (isCurrent) MaterialTheme.apex.accent else MaterialTheme.apex.hairline
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.Hairline, borderColor, RoundedCornerShape(24.dp))
            .alpha(if (isDone) 0.7f else 1f)
            .padding(start = Dimens.Space3, end = Dimens.Space2, top = Dimens.Space2, bottom = Dimens.Space2)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = session.exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (isDone) {
                ExerciseStatusPill(text = "DONE", accent = false)
            } else if (isCurrent) {
                ExerciseStatusPill(text = "NOW", accent = true)
            }
            Box(
                modifier = Modifier
                    .size(Dimens.MinTouchTarget)
                    .apexClickable(onClick = onSwap),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SwapHoriz,
                    contentDescription = "Swap exercise",
                    tint = mutedColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (tip != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tip.muscles.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = mutedColor,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(Dimens.MinTouchTarget)
                        .apexClickable(onClick = { showTip = !showTip }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = if (showTip) "Hide exercise tip" else "Show exercise tip",
                        tint = if (showTip) MaterialTheme.apex.accentText else mutedColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (showTip) {
                Text(
                    text = tip.tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    modifier = Modifier.padding(end = Dimens.Space1)
                )
                Spacer(modifier = Modifier.height(Dimens.Space1))
            }
        }
        if (previousParts.isNotEmpty()) {
            Text(
                text = previousParts.joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = mutedColor
            )
        }
        if (session.exercise.notes.isNotBlank()) {
            Text(text = session.exercise.notes, style = MaterialTheme.typography.bodySmall, color = mutedColor)
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "SET", style = MaterialTheme.typography.labelSmall, color = mutedColor, modifier = Modifier.width(36.dp))
            Text(text = "REPS", style = MaterialTheme.typography.labelSmall, color = mutedColor, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text(text = UnitPreferences.label(useLbs).uppercase(), style = MaterialTheme.typography.labelSmall, color = mutedColor, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(Dimens.MinTouchTarget + 4.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))

        session.sets.forEachIndexed { index, set ->
            val numberColor = if (set.completed) mutedColor else contentColor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = mutedColor,
                    modifier = Modifier.width(36.dp)
                )
                SessionNumberField(
                    value = set.reps,
                    onValueChange = { set.reps = it },
                    contentColor = numberColor,
                    keyboardType = KeyboardType.Number,
                    description = "Reps, set ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Dimens.Space1))
                SessionNumberField(
                    value = set.weight,
                    onValueChange = { set.weight = it },
                    contentColor = numberColor,
                    keyboardType = KeyboardType.Decimal,
                    description = "Weight, set ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                SetCheck(
                    completed = set.completed,
                    onToggle = {
                        set.completed = !set.completed
                        haptics.toggle(set.completed)
                        if (set.completed) onSetCompleted()
                    }
                )
            }
        }
    }
}

// Set done toggle: 48dp tap area around a 34dp circle that fills with the accent when done
@Composable
private fun SetCheck(completed: Boolean, onToggle: () -> Unit) {
    val apex = MaterialTheme.apex
    val fill by animateColorAsState(
        targetValue = if (completed) apex.accent else Color.Transparent,
        animationSpec = motionTween(Motion.Micro),
        label = "setCheckFill"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (completed) 1f else 0.7f,
        animationSpec = motionSpring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium),
        label = "setCheckScale"
    )
    Box(
        modifier = Modifier
            .size(Dimens.MinTouchTarget)
            .apexClickable(haptic = false, onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .drawBehind { drawRect(fill) }
                .border(
                    1.5.dp,
                    if (completed) apex.accent else apex.mutedText.copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = if (completed) "Set done" else "Mark set done",
                tint = if (completed) apex.onAccent else apex.mutedText,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = checkScale
                        scaleY = checkScale
                    }
            )
        }
    }
}

// Reps and weight are the most important numbers in a workout, so they are big and easy to tap.
// No box around them, just a soft background. The accent outline shows which one is being edited.
@Composable
private fun SessionNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    contentColor: Color,
    keyboardType: KeyboardType,
    description: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val accent = MaterialTheme.apex.accent
    val outline by animateColorAsState(
        targetValue = if (focused) accent else Color.Transparent,
        animationSpec = motionTween(Motion.Micro),
        label = "numberFieldOutline"
    )
    val style = ApexText.Numeral.copy(
        fontSize = 30.sp,
        lineHeight = 34.sp,
        color = contentColor,
        textAlign = TextAlign.Center
    )
    val fieldShape = ApexShapes.small
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        interactionSource = interactionSource,
        modifier = modifier.semantics { contentDescription = description },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(fieldShape)
                    .background(MaterialTheme.colorScheme.background)
                    .border(Dimens.Hairline, outline, fieldShape)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) {
                    Text(text = "–", style = style.copy(color = MaterialTheme.apex.mutedText))
                }
                innerTextField()
            }
        }
    )
}

private fun sampleRoutineForPreview() = Routine(
    id = "preview",
    name = "Chest Day",
    emoji = "💪",
    iconKey = "dumbbell",
    days = listOf("Mon"),
    exercises = listOf(
        RoutineExercise(name = "Bench Press", sets = 3, reps = "8", targetWeight = "60 kg", restSeconds = 90),
        RoutineExercise(name = "Incline Dumbbell Press", sets = 3, reps = "10", targetWeight = "22 kg", restSeconds = 60),
        RoutineExercise(name = "Cable Crossover", sets = 3, reps = "12", notes = "Squeeze at the bottom", restSeconds = 45)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Session - light")
@Composable
fun WorkoutSessionScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        WorkoutSessionScreen(
            navController = rememberNavController(),
            routineId = "preview",
            previewRoutine = sampleRoutineForPreview()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Session - dark")
@Composable
private fun WorkoutSessionScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        WorkoutSessionScreen(
            navController = rememberNavController(),
            routineId = "preview",
            previewRoutine = sampleRoutineForPreview()
        )
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Rest timer")
@Composable
private fun RestDockPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(Dimens.ScreenEdge)) {
            RestDock(
                secondsLeft = 42,
                totalSeconds = 60,
                isPaused = false,
                glassState = rememberGlassState(),
                onPauseResume = {},
                onSkip = {},
                onAdjust = {},
                onPreset = {}
            )
        }
    }
}
