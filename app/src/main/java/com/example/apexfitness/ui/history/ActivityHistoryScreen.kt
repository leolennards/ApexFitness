package com.example.apexfitness.ui.history

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.LoggedExercise
import com.example.apexfitness.data.LoggedSet
import com.example.apexfitness.data.WorkoutLog
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.settings.UnitPreferences
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.MetricBlock
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class HistoryState { Loading, Empty, Content }

// All finished workouts, newest first
@Composable
fun ActivityHistoryScreen(
    navController: NavHostController,
    previewLogs: List<WorkoutLog>? = null
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid

    var logs by remember { mutableStateOf(previewLogs ?: emptyList()) }
    var isLoading by remember { mutableStateOf(previewLogs == null) }
    var logToDelete by remember { mutableStateOf<WorkoutLog?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val useLbs = UnitPreferences.useLbs.collectAsState().value

    val glassState = rememberGlassState()

    LaunchedEffect(uid) {
        if (previewLogs == null && uid != null) {
            FirestoreRepository.observeWorkoutLogs(uid).collect { fetched ->
                logs = fetched.sortedByDescending { it.dateMillis }
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    DeleteLogDialogHost(
        pending = logToDelete,
        onCancel = { logToDelete = null },
        onConfirm = { log ->
            logToDelete = null
            if (uid != null && log.id.isNotBlank()) {
                coroutineScope.launch { runCatching { FirestoreRepository.deleteWorkoutLog(uid, log.id) } }
            }
        }
    )

    val state = when {
        isLoading -> HistoryState.Loading
        logs.isEmpty() -> HistoryState.Empty
        else -> HistoryState.Content
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Activity History",
            label = "EVERY SESSION",
            onBack = { navController.popBackStack() }
        )

        Crossfade(
            targetState = state,
            animationSpec = motionTween(Motion.Standard),
            label = "historyStateCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { shown ->
            when (shown) {
                HistoryState.Loading -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenEdge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
                ) {
                    repeat(5) {
                        SkeletonBlock(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = CardShape
                        )
                    }
                }

                HistoryState.Empty -> EmptyHistoryState()

                HistoryState.Content -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenEdge,
                        end = Dimens.ScreenEdge,
                        bottom = Dimens.Space4
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
                ) {
                    itemsIndexed(logs) { index, log ->
                        WorkoutLogCard(
                            log = log,
                            glassState = glassState,
                            useLbs = useLbs,
                            onDelete = { logToDelete = log },
                            modifier = Modifier.staggeredEntrance(
                                index = index.coerceAtMost(8),
                                key = "history-$index"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteLogDialogHost(
    pending: WorkoutLog?,
    onCancel: () -> Unit,
    onConfirm: (WorkoutLog) -> Unit
) {
    if (pending == null) return
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = ApexShapes.large,
        title = {
            Text(
                text = "Delete this workout?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "It will be removed from your history and stats. This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pending) },
                modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
            ) {
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.apex.errorText
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.Space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        Text(
            text = "No workouts logged yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Finish a workout session and it will show up here, with your time, calories and sets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WorkoutLogCard(
    log: WorkoutLog,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    useLbs: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val completedSets = remember(log) { log.exercises.sumOf { ex -> ex.sets.count { it.completed } } }
    val totalSets = remember(log) { log.exercises.sumOf { it.sets.size } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .apexClickable { expanded = !expanded }
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2 + 4.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.routineName.ifBlank { "Workout" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.apex.mutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(log.dateMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.apex.mutedText
                    )
                }
            }
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(MaterialTheme.apex.accentSoft)
                    .padding(horizontal = Dimens.Space1 + 4.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$completedSets/$totalSets SETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.apex.accentText
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space2))

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space3)) {
            MetricBlock(
                value = "${log.durationMinutes}",
                label = "MIN",
                valueStyle = ApexText.NumeralSmall,
                horizontalAlignment = Alignment.Start
            )
            MetricBlock(
                value = "${log.caloriesBurned}",
                label = "KCAL",
                valueStyle = ApexText.NumeralSmall,
                horizontalAlignment = Alignment.Start
            )
            MetricBlock(
                value = "${log.exercises.size}",
                label = "EXERCISES",
                valueStyle = ApexText.NumeralSmall,
                horizontalAlignment = Alignment.Start
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(Dimens.Space2))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.Hairline)
                    .background(MaterialTheme.apex.hairline)
            )
            Spacer(modifier = Modifier.height(Dimens.Space1))

            if (log.exercises.isEmpty()) {
                Text(
                    text = "No sets were recorded for this workout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.apex.mutedText
                )
            }
            log.exercises.forEach { exercise ->
                Spacer(modifier = Modifier.height(Dimens.Space1))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                exercise.sets.forEach { set ->
                    val weight = UnitPreferences.format(UnitPreferences.fromKg(set.weight, useLbs))
                    val unit = UnitPreferences.label(useLbs)
                    val detail = if (set.weight > 0.0) "${set.reps} x $weight $unit" else "${set.reps} reps"
                    Text(
                        text = "Set ${set.setNumber}   $detail" + if (set.completed) "" else "   (not completed)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (set.completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.apex.mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Space1))
            TextButton(
                onClick = onDelete,
                modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.apex.errorText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Delete workout",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.apex.errorText
                )
            }
        }
    }
}

private fun previewHistoryLogs(): List<WorkoutLog> = listOf(
    WorkoutLog(
        id = "1",
        routineName = "Chest Day",
        dateMillis = System.currentTimeMillis(),
        durationMinutes = 52,
        caloriesBurned = 340,
        exercises = listOf(
            LoggedExercise("Bench Press", listOf(LoggedSet(1, 8, 60.0, true), LoggedSet(2, 8, 60.0, true), LoggedSet(3, 6, 60.0, true))),
            LoggedExercise("Incline Dumbbell Press", listOf(LoggedSet(1, 10, 22.0, true), LoggedSet(2, 10, 22.0, true), LoggedSet(3, 9, 22.0, false)))
        )
    ),
    WorkoutLog(
        id = "2",
        routineName = "Leg Day",
        dateMillis = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
        durationMinutes = 61,
        caloriesBurned = 410,
        exercises = listOf(
            LoggedExercise("Back Squat", listOf(LoggedSet(1, 8, 80.0, true), LoggedSet(2, 8, 80.0, true), LoggedSet(3, 8, 80.0, true)))
        )
    )
)

@Preview(showBackground = true, showSystemUi = true, name = "History light")
@Composable
fun ActivityHistoryScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        ActivityHistoryScreen(navController = rememberNavController(), previewLogs = previewHistoryLogs())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History dark")
@Composable
private fun ActivityHistoryScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        ActivityHistoryScreen(navController = rememberNavController(), previewLogs = previewHistoryLogs())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History empty")
@Composable
private fun ActivityHistoryScreenEmptyPreview() {
    ApexFitnessTheme(darkTheme = false) {
        ActivityHistoryScreen(navController = rememberNavController(), previewLogs = emptyList())
    }
}
