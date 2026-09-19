package com.example.apexfitness.ui.challenges

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.apexfitness.data.ChallengeType

// A challenge the user can start
data class ChallengeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val targetValue: Int,
    val durationDays: Int,
    val icon: ImageVector
)

// All the challenges. They are time-limited, unlike badges which never expire.
val ChallengeTemplates = listOf(
    ChallengeTemplate(
        id = "weekly_3",
        title = "3-Day Week",
        description = "Complete 3 workouts in 7 days",
        type = ChallengeType.WORKOUT_COUNT,
        targetValue = 3,
        durationDays = 7,
        icon = Icons.Outlined.FitnessCenter
    ),
    ChallengeTemplate(
        id = "weekly_5",
        title = "5-Day Week",
        description = "Complete 5 workouts in 7 days",
        type = ChallengeType.WORKOUT_COUNT,
        targetValue = 5,
        durationDays = 7,
        icon = Icons.Outlined.TrendingUp
    ),
    ChallengeTemplate(
        id = "monthly_10",
        title = "Monthly Ten",
        description = "Complete 10 workouts in 30 days",
        type = ChallengeType.WORKOUT_COUNT,
        targetValue = 10,
        durationDays = 30,
        icon = Icons.Outlined.CalendarMonth
    ),
    ChallengeTemplate(
        id = "streak_7",
        title = "7-Day Streak",
        description = "Train 7 days in a row",
        type = ChallengeType.STREAK_DAYS,
        targetValue = 7,
        durationDays = 7,
        icon = Icons.Outlined.LocalFireDepartment
    ),
    ChallengeTemplate(
        id = "streak_14",
        title = "14-Day Streak",
        description = "Train 14 days in a row",
        type = ChallengeType.STREAK_DAYS,
        targetValue = 14,
        durationDays = 14,
        icon = Icons.Outlined.Shield
    )
)
