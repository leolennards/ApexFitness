package com.example.apexfitness.ui.Stats

import androidx.compose.runtime.collectAsState
import com.example.apexfitness.ui.settings.UnitPreferences
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import com.example.apexfitness.ui.theme.MetricBlock
import com.example.apexfitness.ui.theme.rememberHaptics
import kotlin.math.roundToInt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.apexfitness.data.PersonalRecord
import com.example.apexfitness.data.StatsCalculations
import com.example.apexfitness.data.UserProfile
import com.example.apexfitness.data.WorkoutLog
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexProgressBar
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberAnimatedProgress
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import java.util.Calendar

// Stats tab

@Composable
fun StatsPage(
    logs: List<WorkoutLog>,
    personalRecords: List<PersonalRecord>,
    profile: UserProfile?,
    isLoading: Boolean = false,
    onOpenCalendar: () -> Unit = {},
    glassState: com.example.apexfitness.ui.theme.GlassState = rememberGlassState(),
    topContentPadding: Dp = 0.dp,
    bottomContentPadding: Dp = 0.dp,
    listState: LazyListState = rememberLazyListState()
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = motionTween(Motion.Standard),
        label = "statsLoadingCrossfade"
    ) { loading ->
        if (loading) {
            StatsSkeleton(topContentPadding = topContentPadding, bottomContentPadding = bottomContentPadding)
        } else {
            StatsContent(
                logs = logs,
                personalRecords = personalRecords,
                onOpenCalendar = onOpenCalendar,
                glassState = glassState,
                topContentPadding = topContentPadding,
                bottomContentPadding = bottomContentPadding,
                listState = listState
            )
        }
    }
}

@Composable
private fun StatsContent(
    logs: List<WorkoutLog>,
    personalRecords: List<PersonalRecord>,
    onOpenCalendar: () -> Unit,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    topContentPadding: Dp,
    bottomContentPadding: Dp,
    listState: LazyListState
) {
    val weeklyMinutes = remember(logs) { StatsCalculations.last7DaysMinutes(logs) }
    val dayLabels = remember { last7DayLabels() }
    val caloriesThisWeek = remember(logs) { StatsCalculations.caloriesThisWeek(logs) }
    val workoutsThisWeek = remember(logs) { StatsCalculations.workoutsThisWeek(logs) }
    val useLbs = UnitPreferences.useLbs.collectAsState().value
    val weeklyVolume = remember(logs) { StatsCalculations.weeklyVolume(logs) }
    val volumeWeekLabels = remember { volumeTrendLabels(weeklyVolume.size) }
    val muscleGroupBreakdown = remember(logs) { StatsCalculations.muscleGroupBreakdown(logs) }
    val unit = UnitPreferences.label(useLbs)

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
        item(key = "stats-header") {
            StatsHeader(
                onOpenCalendar = onOpenCalendar,
                modifier = Modifier.staggeredEntrance(index = 0, key = "stats-header")
            )
        }

        item(key = "stats-hero") {
            StatsHero(
                volumeThisWeek = UnitPreferences.fromKg(weeklyVolume.lastOrNull() ?: 0.0, useLbs),
                volumeLastWeek = UnitPreferences.fromKg(weeklyVolume.getOrNull(weeklyVolume.size - 2) ?: 0.0, useLbs),
                unit = unit,
                minutes = weeklyMinutes.sum(),
                workouts = workoutsThisWeek,
                calories = caloriesThisWeek,
                modifier = Modifier
                    .padding(top = Dimens.Space1)
                    .staggeredEntrance(index = 1, key = "stats-hero")
            )
        }

        item(key = "stats-weekly") {
            StatsSection(
                title = "Weekly activity",
                subtitle = "Minutes trained, last 7 days",
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 2, key = "stats-weekly")
            ) {
                BarChartCard(
                    values = weeklyMinutes.map { it.toFloat() },
                    labels = dayLabels,
                    minScale = 30f,
                    totalLabel = "MIN THIS WEEK",
                    valueSuffix = "MIN",
                    chartName = "Minutes trained per day",
                    glassState = glassState
                )
            }
        }

        item(key = "stats-volume") {
            StatsSection(
                title = "Training volume",
                subtitle = "Weight × reps on completed sets, per week",
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 3, key = "stats-volume")
            ) {
                BarChartCard(
                    values = weeklyVolume.map { UnitPreferences.fromKg(it, useLbs).toFloat() },
                    labels = volumeWeekLabels,
                    minScale = 1f,
                    totalLabel = "${unit.uppercase()} OVER 8 WEEKS",
                    valueSuffix = unit.uppercase(),
                    chartName = "Training volume per week",
                    glassState = glassState
                )
            }
        }

        item(key = "stats-muscle") {
            StatsSection(
                title = "Muscle focus",
                subtitle = "Completed sets by group, last 4 weeks",
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 4, key = "stats-muscle")
            ) {
                if (muscleGroupBreakdown.isEmpty()) {
                    EmptyStatsCard(
                        text = "Log a workout in the last 4 weeks to see your training focus here.",
                        glassState = glassState
                    )
                } else {
                    MuscleGroupCard(breakdown = muscleGroupBreakdown, glassState = glassState)
                }
            }
        }

        item(key = "stats-records") {
            StatsSection(
                title = "Personal records",
                subtitle = "Your best lifts",
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 5, key = "stats-records")
            ) {
                if (personalRecords.isEmpty()) {
                    EmptyStatsCard(
                        text = "Log a workout and your best lifts will show up here.",
                        glassState = glassState
                    )
                } else {
                    PersonalRecordsCard(records = personalRecords, useLbs = useLbs, glassState = glassState)
                }
            }
        }
    }
}

// ---- Header and hero ----

@Composable
private fun StatsHeader(onOpenCalendar: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "YOUR PROGRESS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onOpenCalendar)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "Open calendar",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// The hero: this week's training volume, how it compares with last week,
// then minutes, workouts and calories as three smaller numbers.
@Composable
private fun StatsHero(
    volumeThisWeek: Double,
    volumeLastWeek: Double,
    unit: String,
    minutes: Int,
    workouts: Int,
    calories: Int,
    modifier: Modifier = Modifier
) {
    val shownVolume by rememberCountUpInt(volumeThisWeek.roundToInt())
    val shownMinutes by rememberCountUpInt(minutes)
    val shownWorkouts by rememberCountUpInt(workouts)
    val shownCalories by rememberCountUpInt(calories)
    // Change against last week, only when there is a last week to compare with
    val change = if (volumeLastWeek > 0.0) ((volumeThisWeek - volumeLastWeek) / volumeLastWeek * 100).roundToInt() else null
    val valueColor = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.apex.mutedText

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "VOLUME THIS WEEK", style = MaterialTheme.typography.labelMedium, color = muted)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = groupThousands(shownVolume), style = ApexText.HeroNumeral, color = valueColor, maxLines = 1)
            Text(
                text = " $unit",
                style = MaterialTheme.typography.titleMedium,
                color = muted,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        if (change != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (change >= 0) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${if (change >= 0) "+" else ""}$change% vs last week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted
                )
            }
        } else {
            Text(text = "Rolling 7 days", style = MaterialTheme.typography.bodyMedium, color = muted)
        }
        Spacer(modifier = Modifier.height(Dimens.Space3))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val small = ApexText.NumeralSmall.copy(fontSize = 30.sp, lineHeight = 34.sp)
            MetricBlock(value = groupThousands(shownMinutes), label = "MINUTES", modifier = Modifier.weight(1f), valueStyle = small, valueColor = valueColor)
            HeroDivider()
            MetricBlock(value = "$shownWorkouts", label = "WORKOUTS", modifier = Modifier.weight(1f), valueStyle = small, valueColor = valueColor)
            HeroDivider()
            MetricBlock(value = groupThousands(shownCalories), label = "KCAL (EST.)", modifier = Modifier.weight(1f), valueStyle = small, valueColor = valueColor)
        }
    }
}

@Composable
private fun HeroDivider() {
    Box(
        modifier = Modifier
            .width(Dimens.Hairline)
            .height(36.dp)
            .background(MaterialTheme.apex.hairline)
    )
}

// ---- Sections ----

@Composable
private fun StatsSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
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
        Spacer(modifier = Modifier.height(Dimens.Space2))
        content()
    }
}

@Composable
private fun EmptyStatsCard(
    text: String,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(Dimens.Space3),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText
        )
    }
}

// ---- Bar chart ----

// Card with a big number and a row of bars. The big number is the total until you tap a bar,
// then it shows that bar's value (tap it again to go back to the total).
// The current day or week is the full accent colour, the rest are lighter. Bars grow from 0 the first time.
@Composable
private fun BarChartCard(
    values: List<Float>,
    labels: List<String>,
    minScale: Float,
    totalLabel: String,
    valueSuffix: String,
    chartName: String,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(minScale)
    val progress = rememberAnimatedProgress(1f)
    val accent = MaterialTheme.apex.accent
    val emptyBar = MaterialTheme.apex.hairline
    val baseline = MaterialTheme.apex.hairline
    val mutedColor = MaterialTheme.apex.mutedText
    val strongColor = MaterialTheme.colorScheme.onSurface
    val haptics = rememberHaptics()
    var selected by remember(values.size) { mutableStateOf<Int?>(null) }
    val highlighted = selected ?: values.lastIndex

    val headline = selected?.let { groupThousands(values[it].roundToInt()) } ?: groupThousands(values.sum().roundToInt())
    val headlineLabel = selected?.let { "$valueSuffix · ${labels.getOrElse(it) { "" }.uppercase()}" } ?: totalLabel
    // TalkBack reads every value, so nothing is only shown as a bar height
    val description = chartName + ": " + values.indices.joinToString(", ") { i ->
        "${labels.getOrElse(i) { "" }} ${values[i].roundToInt()}"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = headline,
                style = ApexText.Numeral,
                color = strongColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text(
                text = headlineLabel,
                style = MaterialTheme.typography.labelSmall,
                color = mutedColor,
                modifier = Modifier.padding(bottom = 8.dp),
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .semantics { contentDescription = description }
                .pointerInput(values.size) {
                    detectTapGestures { offset ->
                        if (values.isEmpty()) return@detectTapGestures
                        val slot = size.width.toFloat() / values.size
                        val index = (offset.x / slot).toInt().coerceIn(0, values.lastIndex)
                        selected = if (selected == index) null else index
                        haptics.tick()
                    }
                }
        ) {
            val slot = size.width / values.size.coerceAtLeast(1)
            val barWidth = minOf(14.dp.toPx(), slot * 0.5f)
            val minBar = 4.dp.toPx()
            val radius = CornerRadius(4.dp.toPx())
            // Faint baseline so empty days still read as "zero", not missing
            drawLine(baseline, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            values.forEachIndexed { index, value ->
                val fraction = (value / maxValue).coerceIn(0f, 1f)
                val fullHeight = if (value <= 0f) minBar else minBar + (size.height - minBar) * fraction
                val barHeight = minBar + (fullHeight - minBar) * progress.value
                val color = when {
                    value <= 0f -> emptyBar
                    index == highlighted -> accent
                    else -> accent.copy(alpha = 0.35f)
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x = slot * index + (slot - barWidth) / 2f, y = size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = radius
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space1))

        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == highlighted) strongColor else mutedColor,
                    maxLines = 1
                )
            }
        }
    }
}

// ---- Muscle groups ----

@Composable
private fun MuscleGroupCard(
    breakdown: List<Pair<String, Int>>,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val maxCount = (breakdown.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        breakdown.forEach { (category, count) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$count",
                        style = ApexText.NumeralSmall.copy(fontSize = 20.sp, lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " sets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.apex.mutedText,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                ApexProgressBar(
                    progress = (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f),
                    height = 6.dp
                )
            }
        }
    }
}

// ---- Personal records ----

// All records in one grouped card with thin dividers
@Composable
private fun PersonalRecordsCard(
    records: List<PersonalRecord>,
    useLbs: Boolean,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
    ) {
        records.forEachIndexed { index, record ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = Dimens.Space3)
                        .fillMaxWidth()
                        .height(Dimens.Hairline)
                        .background(MaterialTheme.apex.hairline)
                )
            }
            PersonalRecordRow(record = record, useLbs = useLbs)
        }
    }
}

@Composable
private fun PersonalRecordRow(record: PersonalRecord, useLbs: Boolean) {
    val weighted = record.bestWeight > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space3, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (weighted) "BEST LIFT" else "BEST SET",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = if (weighted) formatWeight(UnitPreferences.fromKg(record.bestWeight, useLbs)) else "${record.bestReps}",
                style = ApexText.NumeralSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = if (weighted) " ${UnitPreferences.label(useLbs)} × ${record.bestReps}" else " reps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

// ---- Loading skeleton ----

@Composable
private fun StatsSkeleton(topContentPadding: Dp, bottomContentPadding: Dp) {
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
        SkeletonBlock(modifier = Modifier.width(110.dp).height(12.dp))
        SkeletonBlock(modifier = Modifier.width(180.dp).height(32.dp))
        Spacer(modifier = Modifier.height(Dimens.Space1))
        SkeletonBlock(modifier = Modifier.width(120.dp).height(12.dp))
        SkeletonBlock(modifier = Modifier.width(200.dp).height(64.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space3)) {
            repeat(3) { SkeletonBlock(modifier = Modifier.weight(1f).height(48.dp)) }
        }
        Spacer(modifier = Modifier.height(Dimens.Space1))
        SkeletonBlock(modifier = Modifier.width(150.dp).height(22.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(220.dp), shape = CardShape)
    }
}

// 12450 becomes "12,450" (or "12 450", depending on the phone's region)
private fun groupThousands(value: Int): String = "%,d".format(value)

// ---- Helpers ----

private fun last7DayLabels(): List<String> {
    val names = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val cal = Calendar.getInstance()
    val result = mutableListOf<String>()
    for (i in 6 downTo 0) {
        val c = cal.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, -i)
        result.add(names[c.get(Calendar.DAY_OF_WEEK) - 1])
    }
    return result
}

// Labels for the volume chart, oldest first, e.g. "-7w" up to "Now"
private fun volumeTrendLabels(count: Int): List<String> =
    (count - 1 downTo 0).map { weeksAgo -> if (weeksAgo == 0) "Now" else "-${weeksAgo}w" }

private fun formatWeight(weight: Double): String = UnitPreferences.format(weight)

// ---- Previews ----

private fun sampleLogs(): List<WorkoutLog> {
    val now = System.currentTimeMillis()
    val day = 24L * 60 * 60 * 1000
    return listOf(
        WorkoutLog(routineName = "Chest Day", dateMillis = now, durationMinutes = 42, caloriesBurned = 320),
        WorkoutLog(routineName = "Leg Day", dateMillis = now - day, durationMinutes = 55, caloriesBurned = 410),
        WorkoutLog(routineName = "Back & Biceps", dateMillis = now - 2 * day, durationMinutes = 38, caloriesBurned = 290),
        WorkoutLog(routineName = "Chest Day", dateMillis = now - 4 * day, durationMinutes = 45, caloriesBurned = 340),
        WorkoutLog(routineName = "Core", dateMillis = now - 5 * day, durationMinutes = 25, caloriesBurned = 180)
    )
}

private fun sampleRecords(): List<PersonalRecord> = listOf(
    PersonalRecord(exerciseName = "Bench Press", bestWeight = 85.0, bestReps = 5),
    PersonalRecord(exerciseName = "Deadlift", bestWeight = 140.0, bestReps = 3),
    PersonalRecord(exerciseName = "Squat", bestWeight = 100.0, bestReps = 5),
    PersonalRecord(exerciseName = "Pull-ups", bestWeight = 0.0, bestReps = 15)
)

@Preview(showBackground = true, showSystemUi = true, name = "Stats light")
@Composable
private fun StatsPageLightPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatsPage(
                logs = sampleLogs(),
                personalRecords = sampleRecords(),
                profile = UserProfile(name = "Alex Morgan", scheduleDays = listOf("Mon", "Wed", "Fri"))
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Stats dark")
@Composable
private fun StatsPageDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatsPage(
                logs = sampleLogs(),
                personalRecords = sampleRecords(),
                profile = null
            )
        }
    }
}

@Preview(showBackground = true, name = "Stats empty")
@Composable
private fun StatsPageEmptyPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatsPage(logs = emptyList(), personalRecords = emptyList(), profile = null)
        }
    }
}
