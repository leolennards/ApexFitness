package com.example.apexfitness.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.SportsMartialArts
import androidx.compose.ui.graphics.vector.ImageVector

// The icons I use for routines. They replace the old emoji so everything looks the same on every phone.
object RoutineIcons {
    // Order shown in the icon picker: key, icon, label
    val options: List<Triple<String, ImageVector, String>> = listOf(
        Triple("dumbbell", Icons.Outlined.FitnessCenter, "Strength"),
        Triple("run", Icons.Outlined.DirectionsRun, "Run"),
        Triple("cycle", Icons.Outlined.DirectionsBike, "Cycle"),
        Triple("yoga", Icons.Outlined.SelfImprovement, "Yoga"),
        Triple("hiit", Icons.Outlined.Bolt, "HIIT"),
        Triple("boxing", Icons.Outlined.SportsMartialArts, "Boxing"),
        Triple("swim", Icons.Outlined.Pool, "Swim"),
        Triple("cardio", Icons.Outlined.Favorite, "Cardio")
    )

    private val byKey: Map<String, ImageVector> = options.associate { (key, icon, _) -> key to icon }

    // Gets the icon for a key, falls back to the dumbbell
    fun iconFor(iconKey: String): ImageVector = byKey[iconKey] ?: Icons.Outlined.FitnessCenter
}
