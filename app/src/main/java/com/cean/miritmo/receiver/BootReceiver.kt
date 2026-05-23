package com.cean.miritmo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cean.miritmo.repository.AuthRepository
import com.cean.miritmo.repository.HabitRepository
import com.cean.miritmo.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d("BootReceiver", "Boot completed, rescheduling alarms...")
            
            val preferencesManager = com.cean.miritmo.datastore.PreferencesManager(context.applicationContext)
            val authManager = com.cean.miritmo.firebase.AuthManager()
            val firestoreManager = com.cean.miritmo.firebase.FirestoreManager()
            val authRepository = AuthRepository(authManager, firestoreManager, preferencesManager)
            val habitRepository = HabitRepository(firestoreManager)
            val alarmScheduler = AlarmScheduler(context)
            
            val userId = authRepository.getCurrentUserId() ?: return
            
            CoroutineScope(Dispatchers.IO).launch {
                val habitsResult = habitRepository.getHabitsForUser(userId)
                if (habitsResult.isSuccess) {
                    val habits = habitsResult.getOrNull() ?: emptyList()
                    habits.forEach { habit ->
                        alarmScheduler.scheduleHabitAlarm(habit)
                    }
                    Log.d("BootReceiver", "Rescheduled ${habits.size} alarms")
                }
            }
        }
    }
}
