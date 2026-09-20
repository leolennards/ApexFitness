package com.example.apexfitness.ui.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.ui.settings.UnitPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Turns the workout history into a CSV file that opens in Excel or Google Sheets
object CsvExport {
    // Returns null when there are no workouts yet
    suspend fun createWorkoutsFile(context: Context, uid: String, useLbs: Boolean): File? = withContext(Dispatchers.IO) {
        val logs = FirestoreRepository.getAllWorkoutLogs(uid)
        if (logs.isEmpty()) return@withContext null

        val unit = UnitPreferences.label(useLbs)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val builder = StringBuilder()
        builder.append("date,routine,duration_minutes,calories,exercise,set,reps,weight_$unit,completed\n")

        logs.forEach { log ->
            val date = dateFormat.format(Date(log.dateMillis))
            if (log.exercises.isEmpty()) {
                builder.append("$date,${escape(log.routineName)},${log.durationMinutes},${log.caloriesBurned},,,,,\n")
            }
            log.exercises.forEach { exercise ->
                exercise.sets.forEach { set ->
                    val weight = UnitPreferences.format(UnitPreferences.fromKg(set.weight, useLbs))
                    builder.append(
                        "$date,${escape(log.routineName)},${log.durationMinutes},${log.caloriesBurned}," +
                            "${escape(exercise.name)},${set.setNumber},${set.reps},$weight,${set.completed}\n"
                    )
                }
            }
        }

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "apexfitness_workouts.csv")
        file.writeText(builder.toString())
        file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ApexFitness workout history")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Export workouts"))
    }

    // Wraps a value in quotes if it has a comma, quote or new line in it
    private fun escape(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n')
        return if (needsQuotes) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }
}
