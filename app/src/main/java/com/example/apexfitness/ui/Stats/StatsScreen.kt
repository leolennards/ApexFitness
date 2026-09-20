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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    bottomContentPadding: Dp = 0.dp
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = motionTween(Motion.Standard),
        label = "statsLoadingCrossfade"
    ) { loading ->
        if (loading) {
            StatsSkeleton(bottomContentPadding = bottomContentPadding)
        } else {
            StatsContent(
                logs = logs,
                personalRecords = personalRecords,
                onOpenCalendar = onOpenCalendar,
                glassState = glassState,
                bottomContentPadding = bottomContentPadding
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
    bottomContentPadding: Dp
) {
    val weeklyMinutes = remember(logs) { StatsCalculations.last7DaysMinutes(logs) }
    val dayLabels = remember { last7DayLabels() }
    val caloriesThisWeek = remember(logs) { StatsCalculations.caloriesThisWeek(logs) }
    val badgeCount = remember(logs) { listOf(1, 5, 10, 25, 50, 100).count { logs.size >= it } }
    val useLbs = UnitPreferences.useLbs.collectAsState().value
    val weeklyVolume = remember(logs) { StatsCalculations.weeklyVolume(logs) }
    val volumeWeekLabels = remember { volumeTrendLabels(weeklyVolume.size) }
    val muscleGroupBreakdown = remember(logs) { StatsCalculations.muscleGroupBreakdown(logs) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space3,
            bottom = Dimens.Space2 + bottomContentPadding
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
            StatsHeroCard(
                calories = caloriesThisWeek,
                badges = badgeCount,
                glassState = glassState,
                modifier = Modifier.staggeredEntrance(index = 1, key = "stats-hero")
            )
        }

        item(key = "stats-weekly") {
            StatsSection(
                title = "Weekly Activity",
                subtitle = "Minutes trained, last 7 days",
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 2, key = "stats-weekly")
            ) {
                BarChartCard(
                    values = weeklyMinutes.map { it.toFloat() },
                    labels = dayLabels,
                    minScale = 30f,
                    headline = "${weeklyMinutes.sum()}",
                    headlineLabel = "MINUTES",
                    glassState = glassState
                )
            }
        }

        item(key = "stats-volume") {
            StatsSection(
                title = "Training Volume",
                subtitle = "Weight x reps on completed sets, last 8 weeks",
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 3, key = "stats-volume")
            ) {
                BarChartCard(
                    values = weeklyVolume.map { UnitPreferences.fromKg(it, useLbs).toFloat() },
                    labels = volumeWeekLabels,
                    minScale = 1f,
                    headline = "${UnitPreferences.fromKg(weeklyVolume.sum(), useLbs).toInt()}",
                    headlineLabel = "${UnitPreferences.label(useLbs).uppercase()} TOTAL",
                    glassState = glassState
                )
            }
        }

        item(key = "stats-muscle") {
            StatsSection(
                title = "Muscle Group Focus",
                subtitle = "Completed sets by category, last 4 weeks",
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

        item(key = "stats-records-header") {
            StatsSectionHeader(
                title = "Personal Records",
                subtitle = "Your best lifts",
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .staggeredEntrance(index = 5, key = "stats-records-header")
            )
        }

        if (personalRecords.isEmpty()) {
            item(key = "stats-records-empty") {
                EmptyStatsCard(
                    text = "Log a workout and your best lifts will show up here.",
                    glassState = glassState,
                    modifier = Modifier.staggeredEntrance(index = 6, key = "stats-records-empty")
                )
            }
        } else {
            itemsIndexed(personalRecords, key = { _, record -> "record-${record.exerciseName}" }) { index, record ->
                PersonalRecordRow(
                    record = record,
                    glassState = glassState,
                    modifier = Modifier.staggeredEntrance(index = 6 + index, key = "stats-record-${record.exerciseName}")
                )
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
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "Calendar",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// The two big numbers for the week. They count up the first time the screen shows.
@Composable
private fun StatsHeroCard(
    calories: Int,
    badges: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val animatedCalories by rememberCountUpInt(calories)
    val animatedBadges by rememberCountUpInt(badges)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(vertical = Dimens.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeroStat(
            value = "$animatedCalories",
            label = "KCAL THIS WEEK",
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(Dimens.Hairline)
                .height(56.dp)
                .background(MaterialTheme.apex.hairline)
        )
        HeroStat(
            value = "$animatedBadges",
            label = "BADGES EARNED",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = Dimens.Space2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = ApexText.Numeral,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.apex.mutedText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---- Sections ----

@Composable
private fun StatsSectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
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
    }
}

@Composable
private fun StatsSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        StatsSectionHeader(title = title, subtitle = subtitle)
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

// Card with a big total and a row of bars. The bars grow from 0 the first time they show,
// and the last bar (today or this week) uses the full gold colour.
@Composable
private fun BarChartCard(
    values: List<Float>,
    labels: List<String>,
    minScale: Float,
    headline: String,
    headlineLabel: String,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(minScale)
    val progress = rememberAnimatedProgress(1f)
    val accent = MaterialTheme.apex.accent
    val emptyBar = MaterialTheme.apex.hairline
    val mutedColor = MaterialTheme.apex.mutedText
    val currentLabelColor = MaterialTheme.apex.accentText
    val summary = "$headline ${headlineLabel.lowercase()}"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
            .semantics { contentDescription = summary }
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = headline,
                style = ApexText.NumeralSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text(
                text = headlineLabel,
                style = MaterialTheme.typography.labelSmall,
                color = mutedColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            val slot = size.width / values.size.coerceAtLeast(1)
            val barWidth = 10.dp.toPx()
            val minBar = 4.dp.toPx()
            val radius = CornerRadius(barWidth / 2f)
            values.forEachIndexed { index, value ->
                val fraction = (value / maxValue).coerceIn(0f, 1f)
                val fullHeight = if (value <= 0f) minBar else minBar + (size.height - minBar) * fraction
                val barHeight = (minBar + (fullHeight - minBar) * progress.value)
                val isCurrent = index == values.lastIndex
                val color = when {
                    value <= 0f -> emptyBar
                    isCurrent -> accent
                    else -> accent.copy(alpha = 0.55f)
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
                    color = if (index == labels.lastIndex) currentLabelColor else mutedColor,
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                        text = "$count sets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.apex.mutedText
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.Space1))
                ApexProgressBar(
                    progress = (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f),
                    height = 6.dp
                )
            }
        }
    }
}

// ---- Personal records ----

@Composable
private fun PersonalRecordRow(
    record: PersonalRecord,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val useLbs = UnitPreferences.useLbs.collectAsState().value
    val result = if (record.bestWeight > 0) {
        "${formatWeight(UnitPreferences.fromKg(record.bestWeight, useLbs))} ${UnitPreferences.label(useLbs)} x ${record.bestReps}"
    } else {
        "${record.bestReps} reps"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(horizontal = Dimens.Space2, vertical = Dimens.Space2),
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
                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (record.bestWeight > 0) "BEST LIFT" else "BEST SET",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space1))
        Text(
            text = result,
            style = ApexText.NumeralSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

// ---- Loading skeleton ----

@Composable
private fun StatsSkeleton(bottomContentPadding: Dp) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space3,
            bottom = Dimens.Space2 + bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        item {
            Column {
                SkeletonBlock(modifier = Modifier.width(110.dp).height(12.dp))
                Spacer(modifier = Modifier.height(Dimens.Space1))
                SkeletonBlock(modifier = Modifier.width(180.dp).height(32.dp))
            }
        }
        item {
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth().height(112.dp),
                shape = CardShape
            )
        }
        item {
            Column(modifier = Modifier.padding(top = Dimens.Space2)) {
                SkeletonBlock(modifier = Modifier.width(150.dp).height(22.dp))
                Spacer(modifier = Modifier.height(Dimens.Space2))
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = CardShape
                )
            }
        }
        item {
            Column(
                modifier = Modifier.padding(top = Dimens.Space2),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
            ) {
                SkeletonBlock(modifier = Modifier.width(170.dp).height(22.dp))
                repeat(3) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = CardShape
                    )
                }
            }
        }
    }
}

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
