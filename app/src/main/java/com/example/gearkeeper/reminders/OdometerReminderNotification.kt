package com.example.gearkeeper.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.gearkeeper.MainActivity
import com.example.gearkeeper.R
import com.example.gearkeeper.ui.navigation.NavRoutes

object OdometerReminderNotification {
    const val EXTRA_OPEN_ROUTE: String = "open_route"
    private const val CHANNEL_ID: String = "odometer_reminders"
    private const val CHANNEL_NAME: String = "Odometer reminders"
    private const val NOTIFICATION_ID: Int = 2901

    fun showPendingVehicleReminder(context: Context, pendingVehicleCount: Int): Unit {
        if (pendingVehicleCount <= 0) {
            cancel(context = context)
            return
        }
        if (!areNotificationsAllowed(context = context)) {
            return
        }
        ensureChannel(context = context)
        val openIntent: Intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_OPEN_ROUTE, NavRoutes.ODOMETER_REMINDERS)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val contentText: String = context.getString(
            R.string.odometer_reminder_notification_body,
            pendingVehicleCount,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.odometer_reminder_notification_title))
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context): Unit {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun areNotificationsAllowed(context: Context): Boolean {
        val managerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
        if (!managerCompat.areNotificationsEnabled()) {
            return false
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        val permissionState: Int = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel(context: Context): Unit {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing: NotificationChannel? = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) {
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.odometer_reminder_notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }
}
