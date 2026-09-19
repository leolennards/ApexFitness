package com.example.apexfitness.ui.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Keeps what the user types across the sign-up and onboarding steps.
// One instance is made in MainActivity and passed to each screen, then saved to Firestore at the end.
class OnboardingFormState {
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var fitnessLevel by mutableStateOf("")
    var goals by mutableStateOf(setOf<String>())
    var scheduleDays by mutableStateOf(setOf<String>())
    var preferredTime by mutableStateOf("Morning")

    fun reset() {
        name = ""
        email = ""
        fitnessLevel = ""
        goals = emptySet()
        scheduleDays = emptySet()
        preferredTime = "Morning"
    }
}
