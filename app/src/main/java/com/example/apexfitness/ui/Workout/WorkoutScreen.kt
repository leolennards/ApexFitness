package com.example.apexfitness.ui.Workout

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.apexfitness.data.DAYS_OF_WEEK
import com.example.apexfitness.data.Routine
import com.example.apexfitness.data.RoutineExercise
import com.example.apexfitness.data.RoutineIcons
import com.example.apexfitness.data.todayDayCode
import com.example.apexfitness.ui.home.fullDayNameFromCode
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.SharedKeys
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.sharedCardBounds
import com.example.apexfitness.ui.theme.staggeredEntrance

// Workout tab: the whole week day by day, built from the routines made in the routine builder.
// Tap a routine to start it, or "Manage" to add, edit or delete routines.

@Composable
fun WorkoutTabPage(
    routines: List<Routine>,
    onStart: (String) -> Unit,
    onManageRoutines: () -> Unit,
    isLoading: Boolean = false,
    glassState: com.example.apexfitness.ui.theme.GlassState = rememberGlassState(),
    bottomContentPadding: Dp = 0.dp
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = motionTween(Motion.Standard),
        label = "workoutTabLoadingCrossfade"
    ) { loading ->
        if (loading) {
            WorkoutTabSkeleton(bottomContentPadding = bottomContentPadding)
        } else {
            WorkoutTabContent(
                routines = routines,
                onStart = onStart,
                onManageRoutines = onManageRoutines,
                glassState = glassState,
                bottomContentPadding = bottomContentPadding
            )
        }
    }
}

@Composable
private fun WorkoutTabContent(
    routines: List<Routine>,
    onStart: (String) -> Unit,
    onManageRoutines: () -> Unit,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    bottomContentPadding: Dp
) {
    val today = remember { todayDayCode() }
    val unscheduled = routines.filter { it.days.isEmpty() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space3,
            bottom = Dimens.Space3 + bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2 - 4.dp)
    ) {
        item(key = "week-header") {
            WeekHeader(
                onManage = onManageRoutines,
                modifier = Modifier
                    .padding(bottom = Dimens.Space1)
                    .staggeredEntrance(index = 0, key = "week-header")
            )
        }

        items(DAYS_OF_WEEK, key = { "week-day-$it" }) { day ->
            val dayRoutines = routines.filter { it.days.contains(day) }
            WeekDayRow(
                day = day,
                isToday = day == today,
                routines = dayRoutines,
                glassState = glassState,
                onStart = onStart,
                modifier = Modifier.staggeredEntrance(
                    index = 1 + DAYS_OF_WEEK.indexOf(day),
                    key = "week-day-$day"
                )
            )
        }

        if (unscheduled.isNotEmpty()) {
            item(key = "week-unscheduled-header") {
                Column(modifier = Modifier.padding(top = Dimens.Space2)) {
                    Text(
                        text = "Unscheduled",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Routines without a day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.apex.mutedText
                    )
                }
            }
            items(unscheduled, key = { "unscheduled-${it.id}" }) { routine ->
                UnscheduledRoutineRow(
                    routine = routine,
                    glassState = glassState,
                    onStart = {
                        SharedKeys.lastRoutine = "tab-unscheduled-${routine.id}"
                        onStart(routine.id)
                    }
                )
            }
        }

        if (routines.isEmpty()) {
            item(key = "week-empty") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.Space4),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No routines yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "Tap Manage to build your first custom routine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.apex.mutedText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekHeader(onManage: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TAP A ROUTINE TO START",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your Week",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Row(
            modifier = Modifier
                .apexClickable(onClick = onManage)
                .heightIn(min = Dimens.MinTouchTarget)
                .clip(PillShape)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, PillShape)
                .padding(horizontal = Dimens.Space2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text(
                text = "Manage",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WeekDayRow(
    day: String,
    isToday: Boolean,
    routines: List<Routine>,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onStart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.apex.accent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .then(
                if (isToday) Modifier.border(Dimens.Hairline, accent, CardShape) else Modifier
            )
            .padding(Dimens.Space2)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(
                        if (isToday) Modifier.background(MaterialTheme.colorScheme.primary)
                        else Modifier.border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.apex.mutedText
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Space2))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullDayNameFromCode(day),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isToday) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.apex.accentText
                    )
                }
            }
            if (routines.isEmpty()) {
                Text(
                    text = "Rest day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText
                )
            }
        }

        if (routines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimens.Space2))
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                routines.forEach { routine ->
                    val sharedKey = "tab-$day-${routine.id}"
                    val rowShape = RoundedCornerShape(16.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedCardBounds(sharedKey)
                            .apexClickable {
                                SharedKeys.lastRoutine = sharedKey
                                onStart(routine.id)
                            }
                            .clip(rowShape)
                            .background(MaterialTheme.colorScheme.background)
                            .heightIn(min = 56.dp)
                            .padding(horizontal = Dimens.Space2),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = RoutineIcons.iconFor(routine.iconKey),
                            contentDescription = null,
                            tint = MaterialTheme.apex.accentText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Space1 + 4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${routine.exercises.size} EXERCISES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.apex.mutedText
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Start ${routine.name}",
                            tint = MaterialTheme.apex.accentText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnscheduledRoutineRow(
    routine: Routine,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sharedCardBounds("tab-unscheduled-${routine.id}")
            .apexClickable(onClick = onStart)
            .glassPanel(glassState, shape = CardShape)
            .heightIn(min = 72.dp)
            .padding(Dimens.Space2),
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
                text = "${routine.exercises.size} EXERCISES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = "Start ${routine.name}",
            tint = MaterialTheme.apex.accentText
        )
    }
}

@Composable
private fun WorkoutTabSkeleton(bottomContentPadding: Dp) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space3,
            bottom = Dimens.Space3 + bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2 - 4.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = Dimens.Space1)) {
                SkeletonBlock(modifier = Modifier.width(170.dp).height(12.dp))
                Spacer(modifier = Modifier.height(Dimens.Space1))
                SkeletonBlock(modifier = Modifier.width(180.dp).height(32.dp))
            }
        }
        items(5) {
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth().height(88.dp),
                shape = CardShape
            )
        }
    }
}

// ---- Previews ----

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
            RoutineExercise(name = "Incline Dumbbell Press", sets = 3, reps = "10")
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
            RoutineExercise(name = "Leg Press", sets = 3, reps = "12"),
            RoutineExercise(name = "Calf Raises", sets = 3, reps = "20")
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

@Preview(showBackground = true, showSystemUi = true, name = "Workout tab light")
@Composable
private fun WorkoutTabPageLightPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            WorkoutTabPage(routines = sampleRoutines(), onStart = {}, onManageRoutines = {})
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Workout tab dark")
@Composable
private fun WorkoutTabPageDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            WorkoutTabPage(routines = sampleRoutines(), onStart = {}, onManageRoutines = {})
        }
    }
}
