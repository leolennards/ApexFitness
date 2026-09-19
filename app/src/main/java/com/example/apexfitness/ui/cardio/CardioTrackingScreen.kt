package com.example.apexfitness.ui.cardio

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Pool
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.CardioCalculations
import com.example.apexfitness.data.CardioLog
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.ApexTextField
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
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Icon for each cardio activity
private fun iconForType(type: String): ImageVector = when (type) {
    "Running" -> Icons.Outlined.DirectionsRun
    "Cycling" -> Icons.Outlined.DirectionsBike
    "Swimming" -> Icons.Outlined.Pool
    "Walking" -> Icons.Outlined.DirectionsWalk
    else -> Icons.Outlined.FitnessCenter
}

// Cardio tracking. It is kept separate from the strength logs because sets and reps do not fit a run or swim.
// Calories come from CardioCalculations and use the profile weight if the user entered one.
@Composable
fun CardioTrackingScreen(
    navController: NavHostController,
    previewLogs: List<CardioLog>? = null
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()
    val glassState = rememberGlassState()

    var logs by remember { mutableStateOf(previewLogs ?: emptyList()) }
    var weightKg by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(previewLogs == null) }
    var showLogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (previewLogs != null) return@LaunchedEffect
        val id = uid
        if (id == null) {
            isLoading = false
            return@LaunchedEffect
        }
        launch {
            FirestoreRepository.observeProfile(id).collect { profile -> weightKg = profile?.weightKg }
        }
        launch {
            FirestoreRepository.observeCardioLogs(id).collect { fetched ->
                logs = fetched.sortedByDescending { it.dateMillis }
                isLoading = false
            }
        }
    }

    fun logSession(type: String, durationMinutes: Int, distanceKm: Double) {
        val id = uid ?: return
        val calories = CardioCalculations.estimateCaloriesBurned(type, durationMinutes, weightKg)
        coroutineScope.launch {
            runCatching {
                FirestoreRepository.addCardioLog(
                    id,
                    CardioLog(
                        type = type,
                        durationMinutes = durationMinutes,
                        distanceKm = distanceKm,
                        caloriesBurned = calories
                    )
                )
            }
        }
    }

    fun deleteSession(logId: String) {
        val id = uid ?: return
        coroutineScope.launch { runCatching { FirestoreRepository.deleteCardioLog(id, logId) } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Cardio",
            label = "KEEP MOVING",
            onBack = { navController.popBackStack() }
        )

        Crossfade(
            targetState = isLoading,
            animationSpec = motionTween(Motion.Standard),
            label = "cardioLoadingCrossfade",
            modifier = Modifier.weight(1f)
        ) { loading ->
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenEdge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
                ) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(128.dp), shape = CardShape)
                    repeat(3) {
                        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(112.dp), shape = CardShape)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenEdge,
                        end = Dimens.ScreenEdge,
                        bottom = Dimens.Space2
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
                ) {
                    item(key = "summary") {
                        WeeklyCardioSummaryCard(
                            logs = logs,
                            glassState = glassState,
                            modifier = Modifier.staggeredEntrance(index = 0, key = "cardio-summary")
                        )
                    }
                    if (logs.isEmpty()) {
                        item(key = "empty") { EmptyCardioState() }
                    } else {
                        item(key = "label") {
                            Text(
                                text = "RECENT SESSIONS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.apex.mutedText,
                                modifier = Modifier.padding(start = 4.dp, top = Dimens.Space1)
                            )
                        }
                        items(logs, key = { it.id }) { log ->
                            val index = logs.indexOf(log).coerceIn(0, 7) + 1
                            CardioLogCard(
                                log = log,
                                glassState = glassState,
                                onDelete = { deleteSession(log.id) },
                                modifier = Modifier
                                    .staggeredEntrance(index = index, key = "cardio-${log.id}")
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }

        if (!isLoading) {
            ApexPrimaryButton(
                text = "Log Session",
                icon = Icons.Outlined.Add,
                onClick = { showLogDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space2)
            )
        }
    }

    if (showLogDialog) {
        LogCardioSessionDialog(
            onDismiss = { showLogDialog = false },
            onConfirm = { type, durationMinutes, distanceKm ->
                logSession(type, durationMinutes, distanceKm)
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun WeeklyCardioSummaryCard(
    logs: List<CardioLog>,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val sessions = remember(logs) { CardioCalculations.sessionsThisWeek(logs) }
    val minutes = remember(logs) { CardioCalculations.minutesThisWeek(logs) }
    val calories = remember(logs) { CardioCalculations.caloriesThisWeek(logs) }
    val animatedSessions by rememberCountUpInt(sessions)
    val animatedMinutes by rememberCountUpInt(minutes)
    val animatedCalories by rememberCountUpInt(calories)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "THIS WEEK",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space2))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricBlock(value = "$animatedSessions", label = "SESSIONS", modifier = Modifier.weight(1f))
            MetricBlock(value = "$animatedMinutes", label = "MINUTES", modifier = Modifier.weight(1f))
            MetricBlock(value = "$animatedCalories", label = "KCAL", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyCardioState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Space4, horizontal = Dimens.Space3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsRun,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        Text(
            text = "No cardio logged yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Log a run, ride, swim or walk and it will show up here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CardioLogCard(
    log: CardioLog,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(start = Dimens.Space2, top = Dimens.Space2, bottom = Dimens.Space2, end = Dimens.Space1)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.apex.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForType(log.type),
                    contentDescription = null,
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Space2))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.type.ifBlank { "Cardio" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(log.dateMillis)),
                    style = MaterialTheme.typography.bodySmall,
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
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete session",
                    tint = MaterialTheme.apex.mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space2))

        Row(
            modifier = Modifier.padding(end = Dimens.Space1),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space3)
        ) {
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
            if (log.distanceKm > 0) {
                MetricBlock(
                    value = formatDistance(log.distanceKm),
                    label = "KM",
                    valueStyle = ApexText.NumeralSmall,
                    horizontalAlignment = Alignment.Start
                )
            }
        }
    }
}

private fun formatDistance(km: Double): String =
    if (km == km.toLong().toDouble()) km.toLong().toString() else String.format(Locale.US, "%.1f", km)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogCardioSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, durationMinutes: Int, distanceKm: Double) -> Unit
) {
    var selectedType by remember { mutableStateOf(CardioCalculations.CardioTypes.first()) }
    var durationInput by remember { mutableStateOf("") }
    var distanceInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = ApexShapes.large,
        title = {
            Text(
                text = "Log Cardio Session",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "ACTIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.apex.mutedText
                )
                Spacer(modifier = Modifier.height(Dimens.Space1))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Space1),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1)
                ) {
                    CardioCalculations.CardioTypes.forEach { type ->
                        ActivityChip(
                            label = type,
                            selected = selectedType == type,
                            onSelect = { selectedType = type }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.Space2))
                ApexTextField(
                    value = durationInput,
                    onValueChange = { durationInput = it.filter { ch -> ch.isDigit() } },
                    label = "Duration (minutes)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    container = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))
                ApexTextField(
                    value = distanceInput,
                    onValueChange = { input -> distanceInput = input.filter { it.isDigit() || it == '.' } },
                    label = "Distance (km, optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    container = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = durationInput.toIntOrNull()?.takeIf { it > 0 }
                    if (duration != null) {
                        onConfirm(selectedType, duration, distanceInput.toDoubleOrNull() ?: 0.0)
                    }
                },
                modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
            ) {
                Text(text = "Save", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apex.accentText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
            ) {
                Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Composable
private fun ActivityChip(label: String, selected: Boolean, onSelect: () -> Unit) {
    val apex = MaterialTheme.apex
    val fill by animateColorAsState(
        targetValue = if (selected) apex.accentSoft else MaterialTheme.colorScheme.background,
        animationSpec = motionTween(Motion.Micro),
        label = "activityChipFill"
    )
    val border by animateColorAsState(
        targetValue = if (selected) apex.accent else apex.hairline,
        animationSpec = motionTween(Motion.Micro),
        label = "activityChipBorder"
    )
    Box(
        modifier = Modifier
            .apexClickable(onClick = onSelect)
            .heightIn(min = Dimens.MinTouchTarget)
            .clip(PillShape)
            .background(fill)
            .border(Dimens.Hairline, border, PillShape)
            .padding(horizontal = Dimens.Space2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) apex.accentText else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun previewLogs(): List<CardioLog> = listOf(
    CardioLog(id = "1", type = "Running", dateMillis = System.currentTimeMillis(), durationMinutes = 32, distanceKm = 5.2, caloriesBurned = 380),
    CardioLog(id = "2", type = "Cycling", dateMillis = System.currentTimeMillis() - 86400000, durationMinutes = 45, distanceKm = 18.0, caloriesBurned = 410),
    CardioLog(id = "3", type = "Swimming", dateMillis = System.currentTimeMillis() - 2 * 86400000, durationMinutes = 25, distanceKm = 0.0, caloriesBurned = 240)
)

@Preview(showBackground = true, showSystemUi = true, name = "Cardio light")
@Composable
fun CardioTrackingScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        CardioTrackingScreen(navController = rememberNavController(), previewLogs = previewLogs())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Cardio dark")
@Composable
private fun CardioTrackingScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        CardioTrackingScreen(navController = rememberNavController(), previewLogs = previewLogs())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Cardio empty")
@Composable
private fun CardioTrackingScreenEmptyPreview() {
    ApexFitnessTheme(darkTheme = false) {
        CardioTrackingScreen(navController = rememberNavController(), previewLogs = emptyList())
    }
}
