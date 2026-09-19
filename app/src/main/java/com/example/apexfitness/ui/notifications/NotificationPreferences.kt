package com.example.apexfitness.ui.notifications

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Reminder settings saved on the device (not in Firestore), same idea as ThemePreferences
object NotificationPreferences {
    private const val PREFS_NAME = "apex_notification_prefs"
    private const val KEY_ENABLED = "reminders_enabled"
    private const val KEY_HOUR = "reminder_hour"
    private const val KEY_MINUTE = "reminder_minute"
    private const val KEY_DAYS = "reminder_days"

    private const val DEFAULT_HOUR = 18
    private const val DEFAULT_MINUTE = 0

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled

    private val _reminderHour = MutableStateFlow(DEFAULT_HOUR)
    val reminderHour: StateFlow<Int> = _reminderHour

    private val _reminderMinute = MutableStateFlow(DEFAULT_MINUTE)
    val reminderMinute: StateFlow<Int> = _reminderMinute

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _enabled.value = prefs.getBoolean(KEY_ENABLED, false)
        _reminderHour.value = prefs.getInt(KEY_HOUR, DEFAULT_HOUR)
        _reminderMinute.value = prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        _enabled.value = enabled
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        _reminderHour.value = hour
        _reminderMinute.value = minute
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
    }

    // Saves which days are set, so they can be set again after a reboot
    fun saveScheduledDays(context: Context, days: Set<Int>) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DAYS, days.joinToString(","))
            .apply()
    }

    fun getScheduledDays(context: Context): Set<Int> {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DAYS, null) ?: return emptySet()
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }
}
