package com.example.apexfitness.ui.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.example.apexfitness.ui.workout.ActiveWorkout
import com.example.apexfitness.ui.workout.ActiveWorkoutStore
import com.example.apexfitness.ui.home.MainActivity

// The notification that stays up during a workout, with the elapsed time or the rest countdown.
// The countdown is drawn by Android itself, so it keeps ticking with the screen off.
// When the app is in the background, an alarm pings when the rest is over.
object WorkoutSessionNotifier {
    private const val SESSION_CHANNEL_ID = "workout_session"
    private const val REST_CHANNEL_ID = "rest_timer"
    private const val SESSION_NOTIFICATION_ID = 7100
    private const val REST_DONE_NOTIFICATION_ID = 7101
    private const val REST_ALARM_REQUEST_CODE = 7102

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(SESSION_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(SESSION_CHANNEL_ID, "Workout in progress", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Shows your workout time and rest timer while you train"
                    setShowBadge(false)
                }
            )
        }
        if (manager.getNotificationChannel(REST_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(REST_CHANNEL_ID, "Rest timer", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Lets you know when your rest is over"
                    enableVibration(true)
                }
            )
        }
    }

    private fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    // Tapping the notification brings the app back. If Android had closed it, it opens the workout again.
    private fun openWorkoutIntent(context: Context, routineId: String, requestCode: Int): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launch.putExtra(MainActivity.EXTRA_DESTINATION, "workoutSession/$routineId")
        return PendingIntent.getActivity(
            context,
            requestCode,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun showOngoing(context: Context, workout: ActiveWorkout) {
        if (!canNotify(context)) return
        createChannels(context)
        val now = System.currentTimeMillis()
        val isResting = workout.restEndMillis > now && workout.restPausedSecondsLeft == 0
        val builder = NotificationCompat.Builder(context, SESSION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(workout.routineName.ifBlank { "Workout" })
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openWorkoutIntent(context, workout.routineId, SESSION_NOTIFICATION_ID))
        if (isResting) {
            builder.setWhen(workout.restEndMillis)
                .setChronometerCountDown(true)
                .setContentText("Resting  ·  ${workout.doneSets} of ${workout.totalSets} sets done")
        } else {
            builder.setWhen(workout.startTimeMillis)
                .setChronometerCountDown(false)
                .setContentText(
                    if (workout.restPausedSecondsLeft > 0) "Rest paused  ·  ${workout.doneSets} of ${workout.totalSets} sets done"
                    else "${workout.doneSets} of ${workout.totalSets} sets done"
                )
        }
        NotificationManagerCompat.from(context).notify(SESSION_NOTIFICATION_ID, builder.build())
    }

    fun cancelAll(context: Context) {
        cancelRestAlarm(context)
        NotificationManagerCompat.from(context).cancel(SESSION_NOTIFICATION_ID)
        NotificationManagerCompat.from(context).cancel(REST_DONE_NOTIFICATION_ID)
    }

    fun cancelRestDone(context: Context) {
        NotificationManagerCompat.from(context).cancel(REST_DONE_NOTIFICATION_ID)
    }

    // False on Android 12+ when "Alarms & reminders" is off (the default on Android 14+),
    // which means the rest alarm can arrive late
    fun canRingOnTime(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    // Opens the system page where the user can turn "Alarms & reminders" on for this app
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        runCatching {
            context.startActivity(
                Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(android.net.Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun restAlarmIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REST_ALARM_REQUEST_CODE,
        Intent(context, RestTimerReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Only set while the app is in the background, so it never doubles up with the in-app beep.
    // No exact-alarm permission, so Android may deliver it a few seconds late.
    @SuppressLint("MissingPermission")
    fun scheduleRestAlarm(context: Context, restEndMillis: Long) {
        if (restEndMillis <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restEndMillis, restAlarmIntent(context))
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restEndMillis, restAlarmIntent(context))
        }
    }

    fun cancelRestAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(restAlarmIntent(context))
    }

    @SuppressLint("MissingPermission")
    fun showRestDone(context: Context, workout: ActiveWorkout) {
        if (!canNotify(context)) return
        createChannels(context)
        val notification = NotificationCompat.Builder(context, REST_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Rest is over")
            .setContentText("Time for your next set")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setTimeoutAfter(60_000L)
            .setContentIntent(openWorkoutIntent(context, workout.routineId, REST_DONE_NOTIFICATION_ID))
            .build()
        NotificationManagerCompat.from(context).notify(REST_DONE_NOTIFICATION_ID, notification)
    }
}

// Fires when a rest ends while the app is in the background
class RestTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ActiveWorkoutStore.init(context)
        val workout = ActiveWorkoutStore.active.value ?: return
        WorkoutSessionNotifier.showRestDone(context, workout)
        // Switch the ongoing notification back to the workout clock
        val cleared = workout.copy(restEndMillis = 0L, restTotalSeconds = 0, restPausedSecondsLeft = 0)
        ActiveWorkoutStore.save(context, cleared)
        WorkoutSessionNotifier.showOngoing(context, cleared)
    }
}
