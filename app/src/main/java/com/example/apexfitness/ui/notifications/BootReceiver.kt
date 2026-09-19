package com.example.apexfitness.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Alarms are cleared when the phone restarts, so this sets the reminders up again from the saved settings
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationScheduler.rescheduleFromSavedPrefs(context)
        }
    }
}
