package com.example.apexfitness.data

// Turns logged workouts into XP and a level. Nothing extra is stored, it is all worked out
// from the workout logs.
object GamificationCalculations {
    private const val WORKOUT_BASE_XP = 50
    private const val XP_PER_COMPLETED_SET = 5

    // Level maths: xpForLevel(L) = LEVEL_XP_STEP * (L - 1) * L. It is a quadratic curve, so each level
    // takes a bit more effort than the last.
    private const val LEVEL_XP_STEP = 150

    // XP for one workout: a flat amount plus a bonus for every completed set
    fun xpForLog(log: WorkoutLog): Int {
        val completedSets = log.exercises.sumOf { exercise -> exercise.sets.count { it.completed } }
        return WORKOUT_BASE_XP + completedSets * XP_PER_COMPLETED_SET
    }

    // Total XP across every logged workout
    fun totalXp(logs: List<WorkoutLog>): Int = logs.sumOf { xpForLog(it) }

    // XP needed to reach a level (level 1 needs 0)
    fun xpForLevel(level: Int): Int {
        val safeLevel = level.coerceAtLeast(1)
        return LEVEL_XP_STEP * (safeLevel - 1) * safeLevel
    }

    // Level for a given amount of XP
    fun levelForXp(xp: Int): Int {
        var level = 1
        while (xpForLevel(level + 1) <= xp) {
            level++
        }
        return level
    }

    // Short title shown next to the level number
    fun levelTitle(level: Int): String = when {
        level < 5 -> "Rookie"
        level < 10 -> "Contender"
        level < 20 -> "Warrior"
        level < 30 -> "Champion"
        else -> "Legend"
    }

    // Current level and how far the user is towards the next one, for the progress bar
    data class LevelProgress(
        val level: Int,
        val totalXp: Int,
        val xpIntoLevel: Int,
        val xpForNextLevel: Int
    ) {
        val progressFraction: Float
            get() = if (xpForNextLevel <= 0) 1f else (xpIntoLevel.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)
    }

    fun levelProgress(logs: List<WorkoutLog>): LevelProgress {
        val xp = totalXp(logs)
        val level = levelForXp(xp)
        val currentLevelFloor = xpForLevel(level)
        val nextLevelFloor = xpForLevel(level + 1)
        return LevelProgress(
            level = level,
            totalXp = xp,
            xpIntoLevel = xp - currentLevelFloor,
            xpForNextLevel = nextLevelFloor - currentLevelFloor
        )
    }
}
