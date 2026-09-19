package com.example.apexfitness.ui.achievements

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

// Routine editor. Every control is at least 48dp tall.
data class AchievementTier(
    val id: String,
    val title: String,
    val description: String,
    val threshold: Int,
    val icon: ImageVector
)

val AchievementTiers = listOf(
    AchievementTier(
        id = "workouts_1",
        title = "First Step",
        description = "Complete your first workout",
        threshold = 1,
        icon = Icons.Outlined.EmojiEvents
    ),
    AchievementTier(
        id = "workouts_5",
        title = "Getting Started",
        description = "Complete 5 workouts",
        threshold = 5,
        icon = Icons.Outlined.WorkspacePremium
    ),
    AchievementTier(
        id = "workouts_10",
        title = "Committed",
        description = "Complete 10 workouts",
        threshold = 10,
        icon = Icons.Outlined.MilitaryTech
    ),
    AchievementTier(
        id = "workouts_25",
        title = "Dedicated",
        description = "Complete 25 workouts",
        threshold = 25,
        icon = Icons.Outlined.Star
    ),
    AchievementTier(
        id = "workouts_50",
        title = "Iron Will",
        description = "Complete 50 workouts",
        threshold = 50,
        icon = Icons.Outlined.LocalFireDepartment
    ),
    AchievementTier(
        id = "workouts_100",
        title = "Centurion",
        description = "Complete 100 workouts",
        threshold = 100,
        icon = Icons.Outlined.Shield
    )
)
