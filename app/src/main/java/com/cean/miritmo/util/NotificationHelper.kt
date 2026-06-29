package com.cean.miritmo.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cean.miritmo.MainActivity
import com.cean.miritmo.R

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    fun showNotification(habitId: String, title: String, message: String, soundUriStr: String?) {
        val soundUri = if (soundUriStr != null) {
            android.net.Uri.parse(soundUriStr)
        } else {
            android.net.Uri.parse("android.resource://${context.packageName}/" + R.raw.custom_notification)
        }
        
        val dynamicChannelId = "habits_channel_${soundUri.hashCode()}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                dynamicChannelId,
                "Recordatorios de Hábitos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordarte tus hábitos"
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Optional: Pass habitId to open specific screen
            putExtra("habitId", habitId)
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, dynamicChannelId)
            .setSmallIcon(R.drawable.ic_monitor_heart) // Ícono de la app
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        notificationManager.notify(habitId.hashCode(), builder.build())
    }
}
