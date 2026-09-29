package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_UPDATES = "channel_event_updates"
    const val CHANNEL_ANNOUNCEMENTS = "channel_venue_announcements"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                "Schedule Updates & Milestones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time updates regarding event timing, delays, and milestones"
                enableVibration(true)
            }

            val announcementsChannel = NotificationChannel(
                CHANNEL_ANNOUNCEMENTS,
                "Venue Announcements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important alerts from event hosts, parking changes, and table assignments"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(updatesChannel)
            notificationManager.createNotificationChannel(announcementsChannel)
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        channelId: String = CHANNEL_ANNOUNCEMENTS,
        isUrgent: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isUrgent) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not granted yet - in-app notification center still captures it
        }
    }
}
