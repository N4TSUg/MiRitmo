package com.cean.miritmo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cean.miritmo.datastore.PreferencesManager
import com.cean.miritmo.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HabitAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra("HABIT_ID") ?: return
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Hábito"

        Log.d("HabitAlarmReceiver", "Alarm fired for habit: $habitName")

        val preferencesManager = PreferencesManager(context)
        val notificationsEnabled = runBlocking {
            preferencesManager.isNotificationsEnabledFlow.first() ?: true
        }

        if (!notificationsEnabled) {
            Log.d("HabitAlarmReceiver", "Notifications disabled by user. Skipping.")
            return
        }

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(
            habitId = habitId,
            title = "¡Es hora de tu hábito!",
            message = "Es momento de: $habitName"
        )
        
        // TODO: In a more advanced implementation we would reschedule the alarm for the next day here,
        // or let the AlarmManager repeat it. For simplicity, we can reschedule via BootReceiver on restart, 
        // and ideally we'd trigger a service to reschedule for the next day.
    }
}
