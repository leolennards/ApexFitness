package com.example.apexfitness.ui.body

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.BodyEntry
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.settings.UnitPreferences
import com.example.apexfitness.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// Body weight and measurements over time. Weight is stored in kg and shown in the unit from Settings.
@Composable
fun BodyTrackingScreen(
    navController: NavHostController,
    previewEntries: List<BodyEntry>? = null
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()
    val glassState = rememberGlassState()
    val useLbs = UnitPreferences.useLbs.collectAsState().value

    var entries by remember { mutableStateOf(previewEntries ?: emptyList()) }
    var isLoading by remember { mutableStateOf(previewEntries == null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (previewEntries != null) return@LaunchedEffect
        val id = uid
        if (id == null) {
            isLoading = false
            return@LaunchedEffect
        }
        FirestoreRepository.observeBodyEntries(id).collect { fetched ->
            entries = fetched.sortedByDescending { it.dateMillis }
            isLoading = false
        }
    }

    fun saveEntry(entry: BodyEntry) {
        val id = uid ?: return
        coroutineScope.launch {
            runCatching {
                FirestoreRepository.addBodyEntry(id, entry)
                // Keep the profile weight up to date so calorie estimates stay accurate
                if (entry.weightKg > 0) {
                    FirestoreRepository.updateProfileFields(id, mapOf("weightKg" to entry.weightKg))
                }
            }
        }
    }

    fun deleteEntry(entryId: String) {
        val id = uid ?: return
        coroutineScope.launch { runCatching { FirestoreRepository.deleteBodyEntry(id, entryId) } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Body Progress",
            label = "TRACK YOUR CHANGE",
            onBack = { navController.popBackStack() }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenEdge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
                ) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(128.dp), shape = CardShape)
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(200.dp), shape = CardShape)
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
                    if (entries.isEmpty()) {
                        item(key = "empty") { EmptyBodyState() }
                    } else {
                        item(key = "summary") {
                            BodySummaryCard(
                                entries = entries,
                                useLbs = useLbs,
                                glassState = glassState,
                                modifier = Modifier.staggeredEntrance(index = 0, key = "body-summary")
                            )
                        }
                        item(key = "chart") {
                            WeightTrendCard(
                                entries = entries,
                                useLbs = useLbs,
                                glassState = glassState,
                                modifier = Modifier.staggeredEntrance(index = 1, key = "body-chart")
                            )
                        }
                        item(key = "label") {
                            Text(
                                text = "CHECK-INS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.apex.mutedText,
                                modifier = Modifier.padding(start = 4.dp, top = Dimens.Space1)
                            )
                        }
                        items(entries, key = { it.id }) { entry ->
                            BodyEntryCard(
                                entry = entry,
                                useLbs = useLbs,
                                glassState = glassState,
                                onDelete = { deleteEntry(entry.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }

        if (!isLoading) {
            ApexPrimaryButton(
                text = "Log Check-in",
                icon = Icons.Outlined.Add,
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space2)
            )
        }
    }

    if (showDialog) {
        LogBodyDialog(
            useLbs = useLbs,
            onDismiss = { showDialog = false },
            onConfirm = { entry ->
                saveEntry(entry)
                showDialog = false
            }
        )
    }
}

@Composable
private fun BodySummaryCard(
    entries: List<BodyEntry>,
    useLbs: Boolean,
    glassState: GlassState,
    modifier: Modifier = Modifier
) {
    // entries are newest first, and only check-ins with a weight count here
    val weights = remember(entries) { entries.filter { it.weightKg > 0 } }
    val latest = weights.firstOrNull()
    val first = weights.lastOrNull()
    val unit = UnitPreferences.label(useLbs)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "CURRENT WEIGHT",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        if (latest == null) {
            Text(
                text = "No weight logged yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText
            )
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = UnitPreferences.format(UnitPreferences.fromKg(latest.weightKg, useLbs)),
                    style = ApexText.HeroNumeral,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.apex.mutedText,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            if (first != null && first.id != latest.id) {
                val delta = UnitPreferences.fromKg(latest.weightKg - first.weightKg, useLbs)
                val sign = if (delta > 0) "+" else if (delta < 0) "-" else ""
                Spacer(modifier = Modifier.height(Dimens.Space1))
                Text(
                    text = "$sign${UnitPreferences.format(abs(delta))} $unit since your first check-in",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.apex.accentText
                )
            }
        }
    }
}

@Composable
private fun WeightTrendCard(
    entries: List<BodyEntry>,
    useLbs: Boolean,
    glassState: GlassState,
    modifier: Modifier = Modifier
) {
    // Oldest first, last 30 check-ins that have a weight
    val points = remember(entries, useLbs) {
        entries.filter { it.weightKg > 0 }
            .sortedBy { it.dateMillis }
            .takeLast(30)
    }
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "WEIGHT TREND",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space2))
        if (points.size < 2) {
            Text(
                text = "Log at least two check-ins with your weight to see the trend.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText
            )
        } else {
            WeightLineChart(values = points.map { UnitPreferences.fromKg(it.weightKg, useLbs).toFloat() })
            Spacer(modifier = Modifier.height(Dimens.Space1))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = dateFormat.format(Date(points.first().dateMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.apex.mutedText
                )
                Text(
                    text = dateFormat.format(Date(points.last().dateMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.apex.mutedText
                )
            }
        }
    }
}

// Simple line chart drawn on a Canvas. The lowest and highest weight fill the height.
@Composable
private fun WeightLineChart(values: List<Float>, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.apex.accent
    val hairline = MaterialTheme.apex.hairline
    val surface = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        if (values.size < 2) return@Canvas
        val minV = values.minOrNull() ?: return@Canvas
        val maxV = values.maxOrNull() ?: return@Canvas
        val range = maxV - minV
        val pad = 10.dp.toPx()
        val chartHeight = size.height - pad * 2
        val stepX = size.width / (values.size - 1)

        // Three faint guidelines
        for (i in 0..2) {
            val y = pad + chartHeight * i / 2f
            drawLine(
                color = hairline,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val points = values.mapIndexed { index, value ->
            val ratio = if (range < 0.001f) 0.5f else (value - minV) / range
            Offset(index * stepX, pad + chartHeight * (1f - ratio))
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = path,
            color = accent,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        points.forEach { drawCircle(color = accent, radius = 3.dp.toPx(), center = it) }
        // The latest point gets a ring so it stands out
        drawCircle(color = surface, radius = 6.dp.toPx(), center = points.last())
        drawCircle(color = accent, radius = 4.5.dp.toPx(), center = points.last())
    }
}

@Composable
private fun BodyEntryCard(
    entry: BodyEntry,
    useLbs: Boolean,
    glassState: GlassState,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault()) }
    val unit = UnitPreferences.label(useLbs)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(start = Dimens.Space2, top = Dimens.Space2, bottom = Dimens.Space2, end = Dimens.Space1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateFormat.format(Date(entry.dateMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText
            )
            if (entry.weightKg > 0) {
                Text(
                    text = "${UnitPreferences.format(UnitPreferences.fromKg(entry.weightKg, useLbs))} $unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            val measurements = listOf(
                "Waist" to entry.waistCm,
                "Chest" to entry.chestCm,
                "Hips" to entry.hipsCm,
                "Arm" to entry.armCm
            ).filter { it.second > 0 }
            if (measurements.isNotEmpty()) {
                Text(
                    text = measurements.joinToString("  ·  ") { "${it.first} ${UnitPreferences.format(it.second)} cm" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.apex.mutedText
                )
            }
        }
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete check-in",
                tint = MaterialTheme.apex.mutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyBodyState(modifier: Modifier = Modifier) {
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
                imageVector = Icons.Outlined.MonitorWeight,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        Text(
            text = "No check-ins yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Log your weight and measurements every week or two, and your progress will show up here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LogBodyDialog(
    useLbs: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (BodyEntry) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }
    var chestInput by remember { mutableStateOf("") }
    var hipsInput by remember { mutableStateOf("") }
    var armInput by remember { mutableStateOf("") }

    fun clean(input: String) = input.filter { it.isDigit() || it == '.' }
    val weight = weightInput.toDoubleOrNull() ?: 0.0
    val waist = waistInput.toDoubleOrNull() ?: 0.0
    val chest = chestInput.toDoubleOrNull() ?: 0.0
    val hips = hipsInput.toDoubleOrNull() ?: 0.0
    val arm = armInput.toDoubleOrNull() ?: 0.0
    val canSave = weight > 0 || waist > 0 || chest > 0 || hips > 0 || arm > 0
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = ApexShapes.large,
        title = {
            Text(
                text = "Log Check-in",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                ApexTextField(
                    value = weightInput,
                    onValueChange = { weightInput = clean(it) },
                    label = "Weight (${UnitPreferences.label(useLbs)})",
                    keyboardOptions = decimal,
                    container = MaterialTheme.colorScheme.background
                )
                Text(
                    text = "MEASUREMENTS IN CM (OPTIONAL)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.apex.mutedText,
                    modifier = Modifier.padding(top = Dimens.Space1)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                    ApexTextField(
                        value = waistInput,
                        onValueChange = { waistInput = clean(it) },
                        label = "Waist",
                        keyboardOptions = decimal,
                        container = MaterialTheme.colorScheme.background,
                        modifier = Modifier.weight(1f)
                    )
                    ApexTextField(
                        value = chestInput,
                        onValueChange = { chestInput = clean(it) },
                        label = "Chest",
                        keyboardOptions = decimal,
                        container = MaterialTheme.colorScheme.background,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                    ApexTextField(
                        value = hipsInput,
                        onValueChange = { hipsInput = clean(it) },
                        label = "Hips",
                        keyboardOptions = decimal,
                        container = MaterialTheme.colorScheme.background,
                        modifier = Modifier.weight(1f)
                    )
                    ApexTextField(
                        value = armInput,
                        onValueChange = { armInput = clean(it) },
                        label = "Arm",
                        keyboardOptions = decimal,
                        container = MaterialTheme.colorScheme.background,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onConfirm(
                        BodyEntry(
                            weightKg = UnitPreferences.toKg(weight, useLbs),
                            waistCm = waist,
                            chestCm = chest,
                            hipsCm = hips,
                            armCm = arm
                        )
                    )
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

private fun previewEntries(): List<BodyEntry> {
    val day = 24L * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    return listOf(
        BodyEntry(id = "1", dateMillis = now, weightKg = 78.4, waistCm = 82.0, armCm = 36.5),
        BodyEntry(id = "2", dateMillis = now - 7 * day, weightKg = 79.1, waistCm = 83.0),
        BodyEntry(id = "3", dateMillis = now - 14 * day, weightKg = 80.2),
        BodyEntry(id = "4", dateMillis = now - 21 * day, weightKg = 81.0)
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Body light")
@Composable
private fun BodyTrackingScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        BodyTrackingScreen(navController = rememberNavController(), previewEntries = previewEntries())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Body dark")
@Composable
private fun BodyTrackingScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        BodyTrackingScreen(navController = rememberNavController(), previewEntries = previewEntries())
    }
}
