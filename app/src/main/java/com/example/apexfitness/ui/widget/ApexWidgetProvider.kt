package com.example.apexfitness.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.apexfitness.R
import com.example.apexfitness.ui.home.MainActivity

// Home screen widget with two buttons. It shows no live data, it only opens the app on a screen.
class ApexWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_apex)
        views.setOnClickPendingIntent(R.id.widget_root, openApp(context, "main", 0))
        views.setOnClickPendingIntent(R.id.widget_workout, openApp(context, "routines", 1))
        views.setOnClickPendingIntent(R.id.widget_water, openApp(context, "waterTracking", 2))
        return views
    }

    // Each button needs its own request code, otherwise Android treats the intents as the same one
    private fun openApp(context: Context, destination: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DESTINATION, destination)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
