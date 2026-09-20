package com.example.apexfitness.ui.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Kilograms or pounds, saved on the device.
// Everything in Firestore stays in kg. I only convert when a weight is shown or typed in.
object UnitPreferences {
    private const val PREFS_NAME = "apex_unit_prefs"
    private const val KEY_USE_LBS = "use_lbs"
    private const val KG_PER_LB = 0.45359237

    private val _useLbs = MutableStateFlow(false)
    val useLbs: StateFlow<Boolean> = _useLbs

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _useLbs.value = prefs.getBoolean(KEY_USE_LBS, false)
    }

    fun setUseLbs(context: Context, useLbs: Boolean) {
        _useLbs.value = useLbs
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_USE_LBS, useLbs)
            .apply()
    }

    fun label(useLbs: Boolean): String = if (useLbs) "lb" else "kg"

    fun fromKg(kg: Double, useLbs: Boolean): Double = if (useLbs) kg / KG_PER_LB else kg

    // Rounded to 2 decimals so the stored number stays tidy
    fun toKg(value: Double, useLbs: Boolean): Double {
        val kg = if (useLbs) value * KG_PER_LB else value
        return Math.round(kg * 100) / 100.0
    }

    // One decimal at most, and no ".0", e.g. 60.0 -> "60" and 132.3 -> "132.3"
    fun format(value: Double): String {
        val rounded = Math.round(value * 10) / 10.0
        return if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()}" else "$rounded"
    }
}
