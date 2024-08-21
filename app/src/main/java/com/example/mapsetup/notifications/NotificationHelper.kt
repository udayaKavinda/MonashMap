package com.example.mapsetup.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.mapsetup.other.Constants

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
    }

    fun getNotification(): Notification {
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_delete) // Use your app's notification icon
            .setContentTitle("Tracking and Weather Services")
            .setContentText("Tracking location and fetching weather data")
            .setOngoing(true)
            .build()
    }

    fun notify(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
