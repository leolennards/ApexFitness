package com.example.apexfitness.data

import java.util.UUID

// Days of the week in the order I use them everywhere
val DAYS_OF_WEEK = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// Today as a 3-letter code from DAYS_OF_WEEK, e.g. "Mon"
fun todayDayCode(): String {
    val cal = java.util.Calendar.getInstance()
    return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "Mon"
        java.util.Calendar.TUESDAY -> "Tue"
        java.util.Calendar.WEDNESDAY -> "Wed"
        java.util.Calendar.THURSDAY -> "Thu"
        java.util.Calendar.FRIDAY -> "Fri"
        java.util.Calendar.SATURDAY -> "Sat"
        else -> "Sun"
    }
}

// The user's profile, saved at users/{uid}.
// Firestore needs a no-argument constructor, so every property must keep a default value.
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val fitnessLevel: String = "",
    val goals: List<String> = emptyList(),
    val scheduleDays: List<String> = emptyList(),
    val preferredTime: String = "",
    val joinDateMillis: Long = System.currentTimeMillis(),
    val onboardingComplete: Boolean = false,
    // Achievement ids that already showed their celebration
    val acknowledgedAchievementIds: List<String> = emptyList(),
    // Exercises the user starred for quick access
    val favoriteExerciseNames: List<String> = emptyList(),
    // Highest level whose level-up popup was already shown
    val acknowledgedLevel: Int = 1,
    // Daily water goal in ml
    val dailyWaterGoalMl: Int = 2500,
    val weightKg: Double = 0.0
)

// One exercise inside a routine (the plan, not what happened at the gym)
data class RoutineExercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val sets: Int = 3,
    val reps: String = "10",
    val targetWeight: String = "",
    val restSeconds: Int = 60,
    val notes: String = "",
    val order: Int = 0
)

// A custom routine, optionally assigned to some days of the week
data class Routine(
    val id: String = "",
    val name: String = "",
    val emoji: String = "💪",
    val iconKey: String = "dumbbell",
    val days: List<String> = emptyList(),
    val gradientStart: String = "#0F766E",
    val gradientEnd: String = "#14B8A6",
    val exercises: List<RoutineExercise> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

// One set from a live workout, what was actually done
data class LoggedSet(
    val setNumber: Int = 1,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val completed: Boolean = false
)

data class LoggedExercise(
    val name: String = "",
    val sets: List<LoggedSet> = emptyList()
)

// A workout session. Stats, streaks and personal records all come from these.
data class WorkoutLog(
    val id: String = "",
    val routineId: String = "",
    val routineName: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val exercises: List<LoggedExercise> = emptyList()
)

// One day of water intake. The date key is also the document id.
data class WaterLog(
    val dateKey: String = "",
    val millilitersConsumed: Int = 0
)

// One logged cardio session (run, ride, swim and so on)
data class CardioLog(
    val id: String = "",
    val type: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0,
    // 0.0 means no distance was entered
    val distanceKm: Double = 0.0,
    val caloriesBurned: Int = 0
)

// What a challenge's progress is measured against
enum class ChallengeType { WORKOUT_COUNT, STREAK_DAYS }

// A challenge the user started. It is saved under the template id, so starting it again just
// overwrites it with a new start date. The target and duration are copied from the template so
// an active challenge does not change if the catalog changes later.
data class UserChallenge(
    val id: String = "",
    val templateId: String = "",
    val startDateMillis: Long = System.currentTimeMillis(),
    val targetValue: Int = 0,
    val durationDays: Int = 0,
    val completed: Boolean = false,
    val completedAtMillis: Long = 0L
)

// One body check-in. Weight is always saved in kg and measurements in cm. 0 means it was not entered.
data class BodyEntry(
    val id: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val weightKg: Double = 0.0,
    val waistCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val hipsCm: Double = 0.0,
    val armCm: Double = 0.0
)

// Best result for one exercise
data class PersonalRecord(
    val exerciseName: String = "",
    val bestWeight: Double = 0.0,
    val bestReps: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

// Turns an exercise name into a safe Firestore document id
fun slugify(name: String): String =
    name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifEmpty { "exercise" }
