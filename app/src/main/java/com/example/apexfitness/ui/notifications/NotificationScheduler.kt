package com.example.apexfitness.ui.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

// Sets and cancels the weekly reminder alarms with AlarmManager. I did not use WorkManager because
// reminders do not need it, and inexact alarms are fine (a few minutes late does not matter)
// and mean no extra permission is needed.
object NotificationScheduler {
    const val CHANNEL_ID = "workout_reminders"
    const val EXTRA_DAY_OF_WEEK = "extra_day_of_week"
    private const val REQUEST_CODE_BASE = 4200  // plus Calendar.DAY_OF_WEEK (1-7)

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Workout Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders to complete your scheduled workouts"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    // Cancels old alarms and sets one for each day at the chosen time
    fun scheduleAll(context: Context, days: Set<Int>, hour: Int, minute: Int) {
        cancelAll(context)
        NotificationPreferences.saveScheduledDays(context, days)
        days.forEach { dayOfWeek -> scheduleDay(context, dayOfWeek, hour, minute) }
    }

    @SuppressLint("ScheduleExactAlarm", "MissingPermission")
    fun scheduleDay(context: Context, dayOfWeek: Int, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerTime(dayOfWeek, hour, minute)
        val pendingIntent = reminderPendingIntent(context, dayOfWeek)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (dayOfWeek in Calendar.SUNDAY..Calendar.SATURDAY) {
            alarmManager.cancel(reminderPendingIntent(context, dayOfWeek))
        }
    }

    // Sets the alarms again from the saved settings after a reboot
    fun rescheduleFromSavedPrefs(context: Context) {
        NotificationPreferences.init(context)
        if (!NotificationPreferences.enabled.value) return
        val days = NotificationPreferences.getScheduledDays(context)
        if (days.isEmpty()) return
        val hour = NotificationPreferences.reminderHour.value
        val minute = NotificationPreferences.reminderMinute.value
        days.forEach { dayOfWeek -> scheduleDay(context, dayOfWeek, hour, minute) }
    }

    // Turns "Mon".."Sun" into a Calendar.DAY_OF_WEEK value
    fun dayCodeToCalendarDay(code: String): Int? = when (code) {
        "Sun" -> Calendar.SUNDAY
        "Mon" -> Calendar.MONDAY
        "Tue" -> Calendar.TUESDAY
        "Wed" -> Calendar.WEDNESDAY
        "Thu" -> Calendar.THURSDAY
        "Fri" -> Calendar.FRIDAY
        "Sat" -> Calendar.SATURDAY
        else -> null
    }

    private fun reminderPendingIntent(context: Context, dayOfWeek: Int): PendingIntent {
        val intent = Intent(context, WorkoutReminderReceiver::class.java).apply {
            putExtra(EXTRA_DAY_OF_WEEK, dayOfWeek)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + dayOfWeek,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerTime(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (trigger.before(now)) {
            trigger.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }
}
