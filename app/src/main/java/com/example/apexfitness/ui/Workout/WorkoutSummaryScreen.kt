package com.example.apexfitness.ui.Workout

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.ui.settings.UnitPreferences
import com.example.apexfitness.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// The best set for one exercise in a finished workout
data class ExerciseResult(
    val name: String,
    val bestWeightKg: Double,
    val bestReps: Int,
    val isNewRecord: Boolean
)

// Everything the summary screen shows. Weights are in kg and converted when they are displayed.
data class WorkoutSummary(
    val routineName: String,
    val dateMillis: Long,
    val durationMinutes: Int,
    val completedSets: Int,
    val totalSets: Int,
    val volumeKg: Double,
    val calories: Int,
    val exercises: List<ExerciseResult>
)

// The session screen puts the finished workout here and then opens the summary screen
object WorkoutSummaryHolder {
    var current by mutableStateOf<WorkoutSummary?>(null)
}

@Composable
fun WorkoutSummaryScreen(
    navController: NavHostController,
    previewSummary: WorkoutSummary? = null
) {
    val context = LocalContext.current
    val glassState = rememberGlassState()
    val useLbs = UnitPreferences.useLbs.collectAsState().value
    val summary = previewSummary ?: WorkoutSummaryHolder.current

    // If the app was restarted there is nothing to show, so go back
    LaunchedEffect(summary == null) {
        if (summary == null) navController.popBackStack()
    }
    if (summary == null) return

    val unit = UnitPreferences.label(useLbs)
    val records = summary.exercises.filter { it.isNewRecord }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Workout Complete",
            label = dateFormat.format(Date(summary.dateMillis)).uppercase(),
            onBack = { navController.popBackStack() }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = Dimens.ScreenEdge,
                end = Dimens.ScreenEdge,
                bottom = Dimens.Space2
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
        ) {
            item(key = "hero") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(index = 0, key = "summary-hero")
                        .glassPanel(glassState, shape = CardShape)
                        .padding(Dimens.Space3)
                ) {
                    Text(
                        text = summary.routineName.ifBlank { "Workout" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.apex.mutedText
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${summary.durationMinutes}",
                            style = ApexText.HeroNumeral,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "  MIN",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.apex.mutedText,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.Space2))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricBlock(
                            value = "${summary.completedSets}/${summary.totalSets}",
                            label = "SETS",
                            valueStyle = ApexText.NumeralSmall,
                            horizontalAlignment = Alignment.Start
                        )
                        MetricBlock(
                            value = UnitPreferences.format(UnitPreferences.fromKg(summary.volumeKg, useLbs).let { Math.round(it).toDouble() }),
                            label = "VOLUME (${unit.uppercase()})",
                            valueStyle = ApexText.NumeralSmall,
                            horizontalAlignment = Alignment.Start
                        )
                        MetricBlock(
                            value = "${summary.calories}",
                            label = "KCAL",
                            valueStyle = ApexText.NumeralSmall,
                            horizontalAlignment = Alignment.Start
                        )
                    }
                }
            }

            if (records.isNotEmpty()) {
                item(key = "records") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index = 1, key = "summary-records")
                            .glassPanel(glassState, shape = CardShape)
                            .padding(Dimens.Space2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.apex.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.apex.accentText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.padding(start = Dimens.Space1))
                            Text(
                                text = if (records.size == 1) "New personal record" else "${records.size} new personal records",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.Space1))
                        records.forEach { result ->
                            Text(
                                text = "${result.name}: ${resultText(result, useLbs)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.apex.accentText,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (summary.exercises.isNotEmpty()) {
                item(key = "label") {
                    Text(
                        text = "BEST SET PER EXERCISE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.apex.mutedText,
                        modifier = Modifier.padding(start = 4.dp, top = Dimens.Space1)
                    )
                }
                items(summary.exercises) { result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassPanel(glassState, shape = CardShape)
                            .padding(horizontal = Dimens.Space2, vertical = Dimens.Space2),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = resultText(result, useLbs),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.apex.mutedText
                        )
                    }
                }
            }
        }

        ApexPrimaryButton(
            text = "Share",
            icon = Icons.Outlined.Share,
            onClick = { shareSummary(context, summary, useLbs) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space1)
        )
        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenEdge)
                .heightIn(min = Dimens.MinTouchTarget)
        ) {
            Text(text = "Done", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun resultText(result: ExerciseResult, useLbs: Boolean): String =
    if (result.bestWeightKg > 0) {
        "${UnitPreferences.format(UnitPreferences.fromKg(result.bestWeightKg, useLbs))} ${UnitPreferences.label(useLbs)} x ${result.bestReps}"
    } else {
        "${result.bestReps} reps"
    }

// Draws the summary as a picture and opens the share sheet
private fun shareSummary(context: Context, summary: WorkoutSummary, useLbs: Boolean) {
    try {
        val file = createShareImage(context, summary, useLbs)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Just finished ${summary.routineName.ifBlank { "a workout" }} with ApexFitness")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share workout"))
    } catch (e: Exception) {
        // Nothing to do if the picture could not be made or no app can share it
    }
}

// A 1080 x 1350 picture in the app's dark colours
private fun createShareImage(context: Context, summary: WorkoutSummary, useLbs: Boolean): File {
    val width = 1080
    val height = 1350
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val background = 0xFF0E0E11.toInt()
    val textColor = 0xFFF4F1EA.toInt()
    val accent = 0xFFD4B27A.toInt()
    val muted = 0xFF8E8E96.toInt()

    canvas.drawColor(background)

    fun paint(size: Float, color: Int, bold: Boolean = false, spacing: Float = 0f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        letterSpacing = spacing
    }

    val left = 96f
    canvas.drawText("APEXFITNESS", left, 130f, paint(34f, accent, bold = true, spacing = 0.25f))

    val name = summary.routineName.ifBlank { "Workout" }.let { if (it.length > 22) it.take(21) + "..." else it }
    canvas.drawText(name, left, 290f, paint(84f, textColor, bold = true))
    val date = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(summary.dateMillis))
    canvas.drawText(date, left, 360f, paint(38f, muted))

    canvas.drawText("${summary.durationMinutes}", left, 700f, paint(300f, textColor, bold = true))
    canvas.drawText("MINUTES", left, 770f, paint(38f, muted, spacing = 0.2f))

    val unit = UnitPreferences.label(useLbs).uppercase()
    val volume = UnitPreferences.fromKg(summary.volumeKg, useLbs).let { Math.round(it) }
    val columns = listOf(
        "$volume" to "VOLUME ($unit)",
        "${summary.completedSets}" to "SETS",
        "${summary.calories}" to "KCAL"
    )
    columns.forEachIndexed { index, (value, label) ->
        val x = left + index * 300f
        canvas.drawText(value, x, 980f, paint(92f, textColor, bold = true))
        canvas.drawText(label, x, 1035f, paint(30f, muted, spacing = 0.15f))
    }

    val recordCount = summary.exercises.count { it.isNewRecord }
    if (recordCount > 0) {
        val text = if (recordCount == 1) "1 NEW PERSONAL RECORD" else "$recordCount NEW PERSONAL RECORDS"
        canvas.drawText(text, left, 1140f, paint(40f, accent, bold = true, spacing = 0.1f))
    }

    canvas.drawText("Tracked with ApexFitness", left, 1270f, paint(32f, muted))

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "workout_summary.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return file
}

private fun previewSummary() = WorkoutSummary(
    routineName = "Push Day",
    dateMillis = System.currentTimeMillis(),
    durationMinutes = 52,
    completedSets = 11,
    totalSets = 12,
    volumeKg = 6420.0,
    calories = 310,
    exercises = listOf(
        ExerciseResult("Bench Press", 85.0, 5, true),
        ExerciseResult("Overhead Press", 50.0, 8, false),
        ExerciseResult("Tricep Pushdown", 35.0, 12, false)
    )
)

@Preview(showBackground = true, showSystemUi = true, name = "Summary light")
@Composable
private fun WorkoutSummaryScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        WorkoutSummaryScreen(navController = rememberNavController(), previewSummary = previewSummary())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Summary dark")
@Composable
private fun WorkoutSummaryScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        WorkoutSummaryScreen(navController = rememberNavController(), previewSummary = previewSummary())
    }
}
