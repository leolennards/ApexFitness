package com.example.apexfitness.ui.settings

import android.content.Context

// Tracks small one-time "have they seen this yet" flags for first-time guidance,
// so tips like the workout terminology card only show once and then get out of the way.
object OnboardingPreferences {
    private const val PREFS_NAME = "apex_onboarding_prefs"
    private const val KEY_SEEN_WORKOUT_GLOSSARY = "seen_workout_glossary"

    fun hasSeenWorkoutGlossary(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SEEN_WORKOUT_GLOSSARY, false)

    fun setSeenWorkoutGlossary(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SEEN_WORKOUT_GLOSSARY, true)
            .apply()
    }
}
