package com.example.apexfitness.ui.routines

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.DAYS_OF_WEEK
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.Routine
import com.example.apexfitness.data.RoutineExercise
import com.example.apexfitness.data.RoutineIcons
import com.example.apexfitness.data.todayDayCode
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.SharedKeys
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.sharedCardBounds
import com.example.apexfitness.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch

// Weekly view of every routine, grouped by the day it is assigned to.
// If previewRoutines is set it is used instead of Firestore.
@Composable
fun RoutineListScreen(navController: NavHostController, previewRoutines: List<Routine>? = null) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()

    var routines by remember { mutableStateOf<List<Routine>>(previewRoutines.orEmpty()) }
    var isLoading by remember { mutableStateOf(previewRoutines == null) }
    var routineToDelete by remember { mutableStateOf<Routine?>(null) }

    LaunchedEffect(uid) {
        if (previewRoutines != null) {
            routines = previewRoutines
            isLoading = false
        } else if (uid == null) {
            isLoading = false
        } else {
            FirestoreRepository.observeRoutines(uid).collect {
                routines = it
                isLoading = false
            }
        }
    }

    val today = remember { todayDayCode() }
    val unscheduled = routines.filter { it.days.isEmpty() }
    val glassState = rememberGlassState()

    // 0 = loading, 1 = empty, 2 = list
    val contentState = when {
        isLoading -> 0
        routines.isEmpty() -> 1
        else -> 2
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RoutinesHeader(
                onNew = { navController.navigate("routineEditor/new") },
                modifier = Modifier
                    .padding(horizontal = Dimens.ScreenEdge)
                    .padding(top = Dimens.Space3, bottom = Dimens.Space2)
                    .staggeredEntrance(index = 0, key = "routines-header")
            )

            Crossfade(
                targetState = contentState,
                animationSpec = motionTween(Motion.Standard),
                label = "routinesContentCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                when (state) {
                    0 -> RoutinesSkeleton()
                    1 -> EmptyRoutinesState(onCreate = { navController.navigate("routineEditor/new") })
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Dimens.ScreenEdge,
                            end = Dimens.ScreenEdge,
                            top = Dimens.Space1,
                            bottom = Dimens.Space4
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
                    ) {
                        items(DAYS_OF_WEEK.filter { day -> routines.any { it.days.contains(day) } }, key = { "day-$it" }) { day ->
                            DaySection(
                                day = day,
                                isToday = day == today,
                                routines = routines.filter { it.days.contains(day) },
                                glassState = glassState,
                                onEdit = { navController.navigate("routineEditor/${it.id}") },
                                onDelete = { routineToDelete = it },
                                modifier = Modifier
                                    .staggeredEntrance(
                                        index = 1 + DAYS_OF_WEEK.indexOf(day),
                                        key = "routines-day-$day"
                                    )
                            )
                        }

                        if (unscheduled.isNotEmpty()) {
                            item(key = "day-unscheduled") {
                                DaySection(
                                    day = "Unscheduled",
                                    isToday = false,
                                    routines = unscheduled,
                                    glassState = glassState,
                                    onEdit = { navController.navigate("routineEditor/${it.id}") },
                                    onDelete = { routineToDelete = it },
                                    modifier = Modifier.staggeredEntrance(index = 8, key = "routines-day-unscheduled")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val pendingDelete = routineToDelete
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = ApexShapes.large,
            title = {
                Text(
                    text = "Delete ${pendingDelete.name}?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = pendingDelete.id
                        routineToDelete = null
                        if (uid != null) {
                            coroutineScope.launch { runCatching { FirestoreRepository.deleteRoutine(uid, id) } }
                        }
                    },
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
                    onClick = { routineToDelete = null },
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
}

@Composable
private fun RoutinesHeader(onNew: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "BUILT BY YOU, FOR YOU",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "My Routines",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget + 4.dp)
                .apexClickable(onClick = onNew)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "New Routine",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DaySection(
    day: String,
    isToday: Boolean,
    routines: List<Routine>,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onEdit: (Routine) -> Unit,
    onDelete: (Routine) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (day == "Unscheduled") day else fullDayName(day),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (isToday) {
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.apex.accentSoft)
                        .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), PillShape)
                ) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.apex.accentText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)) {
            routines.forEach { routine ->
                val sharedKey = "routinelist-$day-${routine.id}"
                RoutineRow(
                    routine = routine,
                    glassState = glassState,
                    sharedKey = sharedKey,
                    onEdit = {
                        SharedKeys.lastEditor = sharedKey
                        onEdit(routine)
                    },
                    onDelete = { onDelete(routine) }
                )
            }
        }
    }
}

@Composable
private fun RoutineRow(
    routine: Routine,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    sharedKey: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val count = routine.exercises.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sharedCardBounds(sharedKey)
            .apexClickable(onClick = onEdit)
            .glassPanel(glassState, shape = CardShape)
            .heightIn(min = 72.dp)
            .padding(start = Dimens.Space2, top = Dimens.Space1, bottom = Dimens.Space1, end = Dimens.Space1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft),
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count EXERCISE${if (count == 1) "" else "S"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete ${routine.name}",
                tint = MaterialTheme.apex.mutedText
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.apex.mutedText
        )
    }
}

@Composable
private fun RoutinesSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenEdge)
            .padding(top = Dimens.Space1),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2 - 4.dp)
    ) {
        SkeletonBlock(modifier = Modifier.width(140.dp).height(24.dp))
        repeat(4) {
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = CardShape
            )
        }
    }
}

@Composable
private fun EmptyRoutinesState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.Space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft)
                .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space3))
        Text(
            text = "No routines yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Build your first custom routine and assign it to a day - like Chest Day on Monday.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.Space3))
        ApexPrimaryButton(
            text = "Create a Routine",
            onClick = onCreate,
            icon = Icons.Outlined.Add
        )
    }
}

fun fullDayName(code: String): String = when (code) {
    "Mon" -> "Monday"
    "Tue" -> "Tuesday"
    "Wed" -> "Wednesday"
    "Thu" -> "Thursday"
    "Fri" -> "Friday"
    "Sat" -> "Saturday"
    "Sun" -> "Sunday"
    else -> code
}

private fun sampleRoutines() = listOf(
    Routine(
        id = "1",
        name = "Chest Day",
        emoji = "💪",
        iconKey = "dumbbell",
        days = listOf("Mon", "Thu"),
        gradientStart = "#1E3A8A",
        gradientEnd = "#2563EB",
        exercises = listOf(
            RoutineExercise(name = "Bench Press", sets = 4, reps = "8"),
            RoutineExercise(name = "Incline Dumbbell Press", sets = 3, reps = "10"),
            RoutineExercise(name = "Cable Crossover", sets = 3, reps = "12")
        )
    ),
    Routine(
        id = "2",
        name = "Leg Day",
        emoji = "⚡",
        iconKey = "dumbbell",
        days = listOf("Tue", "Fri"),
        gradientStart = "#92400E",
        gradientEnd = "#D97706",
        exercises = listOf(
            RoutineExercise(name = "Squats", sets = 4, reps = "10"),
            RoutineExercise(name = "Romanian Deadlift", sets = 3, reps = "10")
        )
    ),
    Routine(
        id = "3",
        name = "Quick Core Finisher",
        emoji = "🔥",
        iconKey = "hiit",
        days = emptyList(),
        gradientStart = "#10B981",
        gradientEnd = "#059669",
        exercises = listOf(RoutineExercise(name = "Plank", sets = 3, reps = "60s"))
    )
)

@Preview(showBackground = true, showSystemUi = true, name = "Routines light")
@Composable
fun RoutineListScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        RoutineListScreen(navController = rememberNavController(), previewRoutines = sampleRoutines())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Routines dark")
@Composable
private fun RoutineListScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        RoutineListScreen(navController = rememberNavController(), previewRoutines = sampleRoutines())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Routines empty")
@Composable
private fun RoutineListScreenEmptyPreview() {
    ApexFitnessTheme(darkTheme = false) {
        RoutineListScreen(navController = rememberNavController(), previewRoutines = emptyList())
    }
}
