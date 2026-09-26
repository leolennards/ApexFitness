package com.example.apexfitness.ui.Workout

import com.example.apexfitness.ui.settings.UnitPreferences
import com.example.apexfitness.ui.settings.OnboardingPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedContent
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

class ExerciseSession(val exercise: RoutineExercise, val sets: SnapshotStateList<SetEntry>)

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
    }
}

@Composable
private fun SessionBody(
    routineName: String,
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
    val statusLabel = when {
        restSecondsLeft > 0 && isRestPaused -> "REST PAUSED"
        restSecondsLeft > 0 -> "RESTING"
        totalExercises == 0 -> "NO EXERCISES"
        currentExerciseIndex < 0 -> "ALL DONE  ·  $doneSets OF $totalSets SETS"
        else -> "EXERCISE $currentExerciseNumber OF $totalExercises  ·  $doneSets OF $totalSets SETS"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // The body scrolls underneath the header
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ScreenEdge,
                top = 96.dp,
                end = Dimens.ScreenEdge,
                bottom = Dimens.Space4
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
        ) {
            if (showGlossary) {
                item {
                    WorkoutGlossaryCard(
                        glassState = glassState,
                        onDismiss = onDismissGlossary,
                        modifier = Modifier.padding(bottom = Dimens.Space1)
                    )
                }
            }
            items(sessionExercises.size) { index ->
                val session = sessionExercises[index]
                val isDone = session.sets.isNotEmpty() && session.sets.all { it.completed }
                val isCurrent = !isDone && index == currentExerciseIndex
                SessionExerciseCard(
                    session = session,
                    glassState = glassState,
                    history = history[session.exercise.name.trim().lowercase()],
                    onSetCompleted = { onSetCompleted(session) },
                    isCurrent = isCurrent,
                    isDone = isDone,
                    modifier = Modifier.staggeredEntrance(index)
                )
            }
        }

        SessionHeader(
            routineName = routineName,
            statusLabel = statusLabel,
            progress = sessionProgress,
            isSaving = isSaving,
            onClose = onClose,
            onFinish = onFinish,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Rest timer pops up after a set is done and goes away when the rest ends or is skipped
        AnimatedVisibility(
            visible = restSecondsLeft > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = Dimens.ScreenEdge, end = Dimens.ScreenEdge, bottom = Dimens.Space3),
            enter = fadeIn(apexTween(motionEnabled, Motion.Standard)) +
                slideInVertically(apexSpring(motionEnabled)) { it / 3 },
            exit = fadeOut(apexTween(motionEnabled, Motion.Micro + 30)) +
                slideOutVertically(apexSpring(motionEnabled)) { it / 3 }
        ) {
            RestTimerOverlay(
                secondsLeft = restSecondsLeft,
                totalSeconds = restTotalSeconds,
                isPaused = isRestPaused,
                glassState = glassState,
                onPauseResume = onPauseResume,
                onSkip = onSkip,
                onAdjust = onAdjust,
                onPreset = onPreset
            )
        }

        // Shown after a set is ticked off. Rest never starts on its own - the user chooses when.
        AnimatedVisibility(
            visible = restPromptSeconds != null && restSecondsLeft <= 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = Dimens.ScreenEdge, end = Dimens.ScreenEdge, bottom = Dimens.Space3),
            enter = fadeIn(apexTween(motionEnabled, Motion.Standard)) +
                slideInVertically(apexSpring(motionEnabled)) { it / 3 },
            exit = fadeOut(apexTween(motionEnabled, Motion.Micro + 30)) +
                slideOutVertically(apexSpring(motionEnabled)) { it / 3 }
        ) {
            RestPromptBar(
                seconds = restPromptSeconds ?: 0,
                glassState = glassState,
                onStart = { onStartRestPrompt(restPromptSeconds ?: 0) },
                onDismiss = onDismissRestPrompt
            )
        }
    }
}

// Header with close, routine name and the Finish button
@Composable
private fun SessionHeader(
    routineName: String,
    statusLabel: String,
    progress: Float,
    isSaving: Boolean,
    onClose: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
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
                    contentDescription = "Close",
                    tint = glassContentColor(),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Space2))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routineName,
                    style = MaterialTheme.typography.titleMedium,
                    color = glassContentColor(),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = glassMutedContentColor()
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Space1))
            ApexPrimaryButton(
                text = if (isSaving) "Saving" else "Finish",
                onClick = onFinish,
                enabled = !isSaving
            )
        }
        ApexProgressBar(progress = progress, height = 2.dp)
    }
}

@Composable
private fun SessionSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = Dimens.ScreenEdge, end = Dimens.ScreenEdge, top = 96.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        repeat(3) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(184.dp), shape = CardShape)
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
            style = MaterialTheme.typography.titleLarge,
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
    val contentColor = glassContentColor()
    val mutedColor = glassMutedContentColor()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(horizontal = Dimens.Space3, vertical = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nice set!",
                style = MaterialTheme.typography.labelMedium,
                color = mutedColor
            )
            Text(
                text = "Rest for ${formatRestTime(seconds)}?",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        }

        Spacer(modifier = Modifier.width(Dimens.Space2))

        TextButton(onClick = onDismiss) {
            Text("Not now")
        }

        Spacer(modifier = Modifier.width(Dimens.Space1))

        Button(
            onClick = onStart,
            modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
        ) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text("Start rest")
        }
    }
}

@Composable
private fun RestTimerOverlay(
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
    val contentColor = glassContentColor()
    val mutedColor = glassMutedContentColor()
    val accentColor = MaterialTheme.apex.accent
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val total = totalSeconds.coerceAtLeast(1)

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
            pulse.animateTo(1.04f, tween(Motion.Micro, easing = FastOutSlowInEasing))
            pulse.animateTo(1f, tween(Motion.Fade, easing = FastOutSlowInEasing))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "REST",
            style = MaterialTheme.typography.labelMedium,
            color = mutedColor
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))

        Box(
            modifier = Modifier
                .size(136.dp)
                .graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
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
            AnimatedContent(
                targetState = secondsLeft,
                transitionSpec = {
                    if (motionEnabled) {
                        (fadeIn(apexTween(motionEnabled, Motion.Micro)) +
                            slideInVertically(apexSpring(motionEnabled)) { it / 3 }) togetherWith
                            (fadeOut(apexTween(motionEnabled, Motion.Micro)) +
                                slideOutVertically(apexSpring(motionEnabled)) { -it / 3 })
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                },
                label = "restDigits"
            ) { seconds ->
                Text(
                    text = formatRestTime(seconds),
                    style = ApexText.Numeral,
                    color = contentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space2))

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space2), verticalAlignment = Alignment.CenterVertically) {
            RestTimerIconButton(icon = Icons.Outlined.Remove, contentDescription = "Subtract 15 seconds", onClick = { onAdjust(-15) })
            RestTimerIconButton(
                icon = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                contentDescription = if (isPaused) "Resume rest" else "Pause rest",
                onClick = onPauseResume,
                emphasized = true
            )
            RestTimerIconButton(icon = Icons.Outlined.SkipNext, contentDescription = "Skip rest", onClick = onSkip)
            RestTimerIconButton(icon = Icons.Outlined.Add, contentDescription = "Add 15 seconds", onClick = { onAdjust(15) })
        }

        Spacer(modifier = Modifier.height(Dimens.Space1))

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
            RestTimerPresets.forEach { presetSeconds ->
                val isActivePreset = totalSeconds == presetSeconds
                // The outer box is the 48dp tap area, the visible pill is smaller
                Box(
                    modifier = Modifier
                        .heightIn(min = Dimens.MinTouchTarget)
                        .apexClickable(onClick = { onPreset(presetSeconds) }),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (isActivePreset) MaterialTheme.apex.accentSoft else Color.Transparent)
                            .border(
                                Dimens.Hairline,
                                if (isActivePreset) accentColor else MaterialTheme.apex.hairline,
                                PillShape
                            )
                            .padding(horizontal = Dimens.Space2, vertical = Dimens.Space1)
                    ) {
                        Text(
                            text = formatRestTime(presetSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActivePreset) MaterialTheme.apex.accentText else mutedColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestTimerIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    emphasized: Boolean = false
) {
    val size = if (emphasized) 56.dp else Dimens.MinTouchTarget
    Box(
        modifier = Modifier
            .size(size)
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
            tint = if (emphasized) MaterialTheme.apex.accentText else glassContentColor(),
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

@Composable
// Small "CURRENT" / "DONE" label shown on an exercise card during a workout
@Composable
private fun ExerciseStatusPill(text: String, accent: Boolean) {
    val accentColor = MaterialTheme.apex.accent
    val mutedColor = glassMutedContentColor()
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(if (accent) MaterialTheme.apex.accentSoft else Color.Transparent)
            .border(Dimens.Hairline, if (accent) accentColor else MaterialTheme.apex.hairline, PillShape)
            .padding(horizontal = Dimens.Space2, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) MaterialTheme.apex.accentText else mutedColor
        )
    }
}

@Composable
private fun SessionExerciseCard(
    session: ExerciseSession,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onSetCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    history: ExerciseHistory? = null,
    isCurrent: Boolean = false,
    isDone: Boolean = false
) {
    val contentColor = glassContentColor()
    val mutedColor = glassMutedContentColor()
    val haptics = rememberHaptics()
    val useLbs = UnitPreferences.useLbs.collectAsState().value
    val tip = remember(session.exercise.name) { ExerciseInfo.find(session.exercise.name) }
    // Auto-open the form tip for whichever exercise you're currently on, since that's the one
    // a beginner actually needs guidance for right now. Still collapsible by hand.
    var showTip by remember { mutableStateOf(isCurrent) }
    LaunchedEffect(isCurrent) {
        if (isCurrent) showTip = true
    }

    // "80 kg x 8" for a weighted set, "12 reps" for bodyweight
    fun describe(weightKg: Double, reps: Int): String =
        if (weightKg > 0) {
            "${UnitPreferences.format(UnitPreferences.fromKg(weightKg, useLbs))} ${UnitPreferences.label(useLbs)} x $reps"
        } else {
            "$reps reps"
        }
    val previousParts = buildList {
        history?.lastSet?.let { add("Last: ${describe(it.weight, it.reps)}") }
        history?.best?.takeIf { it.bestWeight > 0 || it.bestReps > 0 }?.let { add("Best: ${describe(it.bestWeight, it.bestReps)}") }
    }

    val accentColor = MaterialTheme.apex.accent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .then(
                if (isCurrent) {
                    Modifier.border(1.dp, accentColor, CardShape)
                } else {
                    Modifier
                }
            )
            .alpha(if (isDone) 0.6f else 1f)
            .padding(Dimens.Space3)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = session.exercise.name,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (isDone) {
                ExerciseStatusPill(text = "DONE", accent = false)
            } else if (isCurrent) {
                ExerciseStatusPill(text = "CURRENT", accent = true)
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
                        contentDescription = "Exercise tip",
                        tint = if (showTip) MaterialTheme.apex.accentText else mutedColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (showTip) {
                Text(text = tip.tip, style = MaterialTheme.typography.bodySmall, color = contentColor)
                Spacer(modifier = Modifier.height(Dimens.Space1))
            }
        }
        if (previousParts.isNotEmpty()) {
            Text(
                text = previousParts.joinToString("   ·   "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.accentText
            )
        }
        if (session.exercise.notes.isNotBlank()) {
            Text(text = session.exercise.notes, style = MaterialTheme.typography.bodySmall, color = mutedColor)
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "SET", style = MaterialTheme.typography.labelSmall, color = mutedColor, modifier = Modifier.weight(0.5f))
            Text(text = "REPS", style = MaterialTheme.typography.labelSmall, color = mutedColor, modifier = Modifier.weight(1f))
            Text(text = "WEIGHT (${UnitPreferences.label(UnitPreferences.useLbs.collectAsState().value).uppercase()})", style = MaterialTheme.typography.labelSmall, color = mutedColor, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(Dimens.MinTouchTarget))
        }
        Spacer(modifier = Modifier.height(Dimens.Space1))

        session.sets.forEachIndexed { index, set ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = mutedColor,
                    modifier = Modifier.weight(0.5f)
                )
                SessionNumberField(
                    value = set.reps,
                    onValueChange = { set.reps = it },
                    contentColor = contentColor,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )
                SessionNumberField(
                    value = set.weight,
                    onValueChange = { set.weight = it },
                    contentColor = contentColor,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )
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

// Set done toggle: 48dp tap area around a 30dp circle that turns gold when done
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
                .size(30.dp)
                .clip(CircleShape)
                .background(fill)
                .border(
                    Dimens.Hairline,
                    if (completed) apex.accent else apex.mutedText.copy(alpha = 0.7f),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    // Reps and weight are the main numbers, so they are big and centred
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier,
        textStyle = ApexText.Numeral.copy(
            fontSize = 18.sp,
            lineHeight = 22.sp,
            color = contentColor,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.apex.accent,
            unfocusedBorderColor = MaterialTheme.apex.hairline,
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor,
            cursorColor = MaterialTheme.apex.accent,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background
        ),
        shape = ApexShapes.small
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
private fun RestTimerOverlayPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(Dimens.ScreenEdge)) {
            RestTimerOverlay(
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
