package com.example.apexfitness.data

// Progress maths for the challenges. It is all worked out from the logged workouts, so only the
// start date and target are saved to Firestore.
object ChallengeCalculations {
    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    fun endDateMillis(challenge: UserChallenge): Long =
        challenge.startDateMillis + challenge.durationDays * DAY_MILLIS

    // True once the challenge window has closed
    fun isExpired(challenge: UserChallenge): Boolean =
        System.currentTimeMillis() >= endDateMillis(challenge)

    // How far along the user is. Streak challenges use the current streak, and I cap it at the target
    // so the bar never goes past 100%.
    fun progressValue(challenge: UserChallenge, type: ChallengeType, logs: List<WorkoutLog>): Int =
        when (type) {
            ChallengeType.WORKOUT_COUNT -> logs.count {
                it.dateMillis >= challenge.startDateMillis && it.dateMillis < endDateMillis(challenge)
            }
            ChallengeType.STREAK_DAYS -> StatsCalculations.currentStreak(logs).coerceAtMost(challenge.targetValue)
        }

    fun isComplete(challenge: UserChallenge, type: ChallengeType, logs: List<WorkoutLog>): Boolean =
        progressValue(challenge, type, logs) >= challenge.targetValue
}
