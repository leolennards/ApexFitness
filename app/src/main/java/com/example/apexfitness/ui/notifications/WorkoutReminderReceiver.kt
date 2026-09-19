package com.example.apexfitness.ui.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.apexfitness.R
import com.example.apexfitness.ui.home.MainActivity

// Runs when a reminder alarm goes off. Shows the notification (if allowed) and sets the same alarm for next week.
class WorkoutReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val dayOfWeek = intent.getIntExtra(NotificationScheduler.EXTRA_DAY_OF_WEEK, -1)

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Time to train")
                .setContentText("Your workout is waiting - let's get it done today.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()

            NotificationManagerCompat.from(context).notify(dayOfWeek, notification)
        }

        // Set it again for next week, an alarm does not repeat by itself
        if (dayOfWeek in 1..7) {
            NotificationPreferences.init(context)
            val hour = NotificationPreferences.reminderHour.value
            val minute = NotificationPreferences.reminderMinute.value
            NotificationScheduler.scheduleDay(context, dayOfWeek, hour, minute)
        }
    }
}
