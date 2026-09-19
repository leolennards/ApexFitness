package com.example.apexfitness.ui.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.apexfitness.data.DAYS_OF_WEEK
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.WorkoutLog
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun dayKey(millis: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

// One month at a glance. Days with a workout are highlighted, tap one to see what was logged.
@Composable
fun CalendarScreen(
    navController: NavHostController,
    previewLogs: List<WorkoutLog>? = null
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid

    var logs by remember { mutableStateOf(previewLogs ?: emptyList()) }
    var isLoading by remember { mutableStateOf(previewLogs == null) }
    var displayedMonth by remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }) }
    var selectedDayKey by remember { mutableStateOf<String?>(null) }

    val glassState = rememberGlassState()

    LaunchedEffect(uid) {
        if (previewLogs == null && uid != null) {
            FirestoreRepository.observeWorkoutLogs(uid).collect { fetched ->
                logs = fetched
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    val workoutDayKeys = remember(logs) { logs.map { dayKey(it.dateMillis) }.toSet() }
    val logsByDayKey = remember(logs) { logs.groupBy { dayKey(it.dateMillis) } }
    val selectedDayLogs = remember(selectedDayKey, logsByDayKey) {
        selectedDayKey?.let { logsByDayKey[it] } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Calendar",
            label = "YOUR CONSISTENCY",
            onBack = { navController.popBackStack() }
        )

        Crossfade(
            targetState = isLoading,
            animationSpec = motionTween(Motion.Standard),
            label = "calendarLoadingCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { loading ->
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenEdge)
                ) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        shape = CardShape
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenEdge,
                        end = Dimens.ScreenEdge,
                        bottom = Dimens.Space4
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
                ) {
                    item(key = "month") {
                        MonthCalendarCard(
                            month = displayedMonth,
                            workoutDayKeys = workoutDayKeys,
                            selectedDayKey = selectedDayKey,
                            glassState = glassState,
                            onPreviousMonth = {
                                displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                                selectedDayKey = null
                            },
                            onNextMonth = {
                                displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                                selectedDayKey = null
                            },
                            onDaySelected = { key -> selectedDayKey = if (selectedDayKey == key) null else key },
                            modifier = Modifier.staggeredEntrance(index = 0, key = "calendar-month")
                        )
                    }

                    if (selectedDayKey != null) {
                        item(key = "selected-label") {
                            Text(
                                text = if (selectedDayLogs.isEmpty()) "NO WORKOUTS LOGGED" else "WORKOUTS LOGGED",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.apex.mutedText,
                                modifier = Modifier
                                    .padding(start = 4.dp, top = Dimens.Space1)
                                    .animateItem()
                            )
                        }
                        itemsIndexed(selectedDayLogs) { index, log ->
                            CalendarLogRow(
                                log = log,
                                glassState = glassState,
                                modifier = Modifier
                                    .staggeredEntrance(index = index.coerceAtMost(6), key = "calendar-log-$selectedDayKey-$index")
                                    .animateItem()
                            )
                        }
                    } else {
                        item(key = "hint") {
                            Text(
                                text = "Tap a highlighted day to see what you logged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.apex.mutedText,
                                modifier = Modifier
                                    .padding(start = 4.dp, top = Dimens.Space1)
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

// Always six rows so the card keeps the same height when changing month
private fun buildMonthCells(month: Calendar): List<List<Pair<Int, String>?>> {
    val firstOfMonth = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val leadingBlanks = (firstOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val list = mutableListOf<Pair<Int, String>?>()
    repeat(leadingBlanks) { list.add(null) }
    for (day in 1..daysInMonth) {
        val c = firstOfMonth.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, day)
        list.add(day to dayKey(c.timeInMillis))
    }
    while (list.size < 42) { list.add(null) }
    return list.chunked(7)
}

@Composable
private fun MonthCalendarCard(
    month: Calendar,
    workoutDayKeys: Set<String>,
    selectedDayKey: String?,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val apex = MaterialTheme.apex
    val todayKey = remember { dayKey(System.currentTimeMillis()) }
    val monthTitleFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(horizontal = Dimens.Space1 + 4.dp, vertical = Dimens.Space2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(Dimens.MinTouchTarget).apexClickable(onClick = onPreviousMonth),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Previous month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = monthTitleFormat.format(month.time),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier.size(Dimens.MinTouchTarget).apexClickable(onClick = onNextMonth),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Next month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space1))

        Row(modifier = Modifier.fillMaxWidth()) {
            DAYS_OF_WEEK.forEach { day ->
                Text(
                    text = day.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = apex.mutedText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space1))

        Crossfade(
            targetState = month,
            animationSpec = motionTween(Motion.Micro),
            label = "calendarMonthCrossfade"
        ) { shownMonth ->
            val cells = remember(shownMonth) { buildMonthCells(shownMonth) }
            Column {
                cells.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { cell ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(Dimens.MinTouchTarget),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell != null) {
                                    val (day, key) = cell
                                    DayCell(
                                        day = day,
                                        hasWorkout = workoutDayKeys.contains(key),
                                        isToday = key == todayKey,
                                        isSelected = key == selectedDayKey,
                                        onClick = { onDaySelected(key) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    hasWorkout: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val apex = MaterialTheme.apex
    val fill by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            hasWorkout -> apex.accentSoft
            else -> apex.accentSoft.copy(alpha = 0f)
        },
        animationSpec = motionTween(Motion.Micro),
        label = "dayCellFill"
    )
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        hasWorkout -> apex.accentText
        else -> apex.mutedText
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (hasWorkout) Modifier.apexClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(fill)
                .then(
                    if (isToday && !isSelected) Modifier.border(Dimens.Hairline, apex.accent, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
private fun CalendarLogRow(log: WorkoutLog, glassState: com.example.apexfitness.ui.theme.GlassState, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.apex.mutedText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2 + 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.routineName.ifBlank { "Workout" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Timer, contentDescription = null, tint = muted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${log.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = muted)
                Spacer(modifier = Modifier.width(Dimens.Space1 + 4.dp))
                Icon(imageVector = Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = muted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${log.caloriesBurned} kcal", style = MaterialTheme.typography.bodySmall, color = muted)
            }
        }
    }
}

private fun previewLogs(): List<WorkoutLog> {
    val now = System.currentTimeMillis()
    val day = 24L * 60 * 60 * 1000
    return listOf(
        WorkoutLog(routineName = "Chest Day", dateMillis = now, durationMinutes = 42, caloriesBurned = 320),
        WorkoutLog(routineName = "Leg Day", dateMillis = now - 2 * day, durationMinutes = 55, caloriesBurned = 410),
        WorkoutLog(routineName = "Back & Biceps", dateMillis = now - 4 * day, durationMinutes = 48, caloriesBurned = 350)
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Calendar light")
@Composable
fun CalendarScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        CalendarScreen(navController = rememberNavController(), previewLogs = previewLogs())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Calendar dark")
@Composable
private fun CalendarScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        CalendarScreen(navController = rememberNavController(), previewLogs = previewLogs())
    }
}
