package com.example.apexfitness.data

import java.util.Calendar
import kotlin.math.roundToInt

// Works out calories for cardio sessions. Same idea as the strength workouts (MET x weight x hours),
// but every activity has its own MET value.
object CardioCalculations {
    private const val DEFAULT_BODY_WEIGHT_KG = 70.0

    private val MetByType = mapOf(
        "Running" to 9.8,
        "Cycling" to 7.5,
        "Swimming" to 8.3,
        "Walking" to 3.8,
        "Other" to 6.0
    )

    // Activities shown in the log dialog, in this order
    val CardioTypes = listOf("Running", "Cycling", "Swimming", "Walking", "Other")

    fun estimateCaloriesBurned(type: String, durationMinutes: Int, weightKg: Double? = null): Int {
        if (durationMinutes <= 0) return 0
        val weight = weightKg?.takeIf { it > 0 } ?: DEFAULT_BODY_WEIGHT_KG
        val met = MetByType[type] ?: MetByType.getValue("Other")
        val hours = durationMinutes / 60.0
        return (met * weight * hours).roundToInt()
    }

    private fun isWithinLastDays(millis: Long, days: Int): Boolean {
        val cutoff = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.timeInMillis
        return millis >= cutoff
    }

    fun sessionsThisWeek(logs: List<CardioLog>): Int = logs.count { isWithinLastDays(it.dateMillis, 7) }

    fun minutesThisWeek(logs: List<CardioLog>): Int =
        logs.filter { isWithinLastDays(it.dateMillis, 7) }.sumOf { it.durationMinutes }

    fun caloriesThisWeek(logs: List<CardioLog>): Int =
        logs.filter { isWithinLastDays(it.dateMillis, 7) }.sumOf { it.caloriesBurned }
}
