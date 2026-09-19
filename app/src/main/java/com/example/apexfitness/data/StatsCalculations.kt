package com.example.apexfitness.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Helpers that turn workout history into the numbers on Home and Stats
object StatsCalculations {

    private const val DEFAULT_BODY_WEIGHT_KG = 70.0

    private fun dayKey(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    // Days in a row with at least one workout, counting back from today (or yesterday if today has none yet)
    fun currentStreak(logs: List<WorkoutLog>): Int {
        if (logs.isEmpty()) return 0
        val days = logs.map { dayKey(it.dateMillis) }.toSet()
        val cal = Calendar.getInstance()
        if (!days.contains(dayKey(cal.timeInMillis))) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var streak = 0
        while (days.contains(dayKey(cal.timeInMillis))) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    fun totalWorkouts(logs: List<WorkoutLog>): Int = logs.size

    private fun isWithinLastDays(millis: Long, days: Int): Boolean {
        val cutoff = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.timeInMillis
        return millis >= cutoff
    }

    fun workoutsThisWeek(logs: List<WorkoutLog>): Int = logs.count { isWithinLastDays(it.dateMillis, 7) }

    fun caloriesThisWeek(logs: List<WorkoutLog>): Int = logs.filter { isWithinLastDays(it.dateMillis, 7) }.sumOf { it.caloriesBurned }

    // Calorie estimate: MET x weight (kg) x hours. The MET goes from 3.5 to 6.0 depending on how busy the
    // session was. If the user has not entered a weight I use 70kg.
    fun estimateCaloriesBurned(durationMinutes: Int, completedSets: Int, weightKg: Double? = null): Int {
        if (durationMinutes <= 0) return 0
        val weight = weightKg?.takeIf { it > 0 } ?: DEFAULT_BODY_WEIGHT_KG
        val hours = durationMinutes / 60.0
        val density = (completedSets.toDouble() / durationMinutes).coerceIn(0.0, 1.0)
        val met = 3.5 + density * 2.5
        return (met * weight * hours).roundToInt()
    }

    // Percentage of this week's planned days that already have a workout
    fun weeklyCompletionPercent(logs: List<WorkoutLog>, scheduledDaysPerWeek: Int): Int {
        val target = scheduledDaysPerWeek.coerceAtLeast(1)
        val completed = workoutsThisWeek(logs)
        return ((completed.toFloat() / target) * 100).toInt().coerceIn(0, 100)
    }

    // Minutes trained on each of the last 7 days, oldest first, for the weekly chart
    fun last7DaysMinutes(logs: List<WorkoutLog>): List<Int> {
        val cal = Calendar.getInstance()
        val result = mutableListOf<Int>()
        val keys = mutableListOf<String>()
        for (i in 6 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            keys.add(dayKey(c.timeInMillis))
        }
        val minutesByDay = logs.groupBy { dayKey(it.dateMillis) }.mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
        keys.forEach { key -> result.add(minutesByDay[key] ?: 0) }
        return result
    }

    // Total weight lifted (weight x reps) for one log, finished sets only
    private fun logVolume(log: WorkoutLog): Double =
        log.exercises.sumOf { ex -> ex.sets.filter { it.completed }.sumOf { it.weight * it.reps } }

    // Total volume for each of the last few weeks, oldest first. These are rolling 7-day blocks, not calendar weeks.
    fun weeklyVolume(logs: List<WorkoutLog>, weeks: Int = 8): List<Double> {
        val now = Calendar.getInstance().timeInMillis
        val msPerWeek = 7L * 24 * 60 * 60 * 1000
        val buckets = DoubleArray(weeks)
        logs.forEach { log ->
            val diff = now - log.dateMillis
            if (diff < 0) return@forEach
            val weekIndex = (diff / msPerWeek).toInt()
            if (weekIndex in 0 until weeks) {
                buckets[weeks - 1 - weekIndex] += logVolume(log)
            }
        }
        return buckets.toList()
    }

    // Completed sets per muscle group over the last few days, most trained first.
    // Exercises that are not in the library go under "Other".
    fun muscleGroupBreakdown(logs: List<WorkoutLog>, days: Int = 28): List<Pair<String, Int>> {
        val cutoff = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }.timeInMillis
        val recentLogs = logs.filter { it.dateMillis >= cutoff }
        val nameToCategory: Map<String, String> = ExerciseLibrary.exercisesByCategory
            .flatMap { (category, names) -> names.map { name -> name.lowercase() to category } }
            .toMap()

        val counts = linkedMapOf<String, Int>()
        recentLogs.forEach { log ->
            log.exercises.forEach { ex ->
                val completedSets = ex.sets.count { it.completed }
                if (completedSets <= 0) return@forEach
                val exerciseNameLower = ex.name.trim().lowercase()
                val category = nameToCategory[exerciseNameLower]
                    ?: nameToCategory.entries.firstOrNull { (libraryName, _) ->
                        exerciseNameLower.isNotBlank() && (exerciseNameLower.contains(libraryName) || libraryName.contains(exerciseNameLower))
                    }?.value
                    ?: "Other"
                counts[category] = (counts[category] ?: 0) + completedSets
            }
        }
        return counts.entries.sortedByDescending { it.value }.map { it.key to it.value }
    }
}
