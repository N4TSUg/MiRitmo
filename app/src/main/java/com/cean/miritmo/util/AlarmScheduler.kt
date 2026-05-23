package com.cean.miritmo.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cean.miritmo.model.Habit
import com.cean.miritmo.receiver.HabitAlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleHabitAlarm(habit: Habit) {
        val targetTimes = habit.getEffectiveTargetTimes()
        if (targetTimes.isEmpty()) return
        
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        
        targetTimes.forEach { timeStr ->
            try {
                val targetDate = timeFormat.parse(timeStr) ?: return@forEach
                
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    
                    val parsedCalendar = Calendar.getInstance().apply { time = targetDate }
                    
                    set(Calendar.HOUR_OF_DAY, parsedCalendar.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, parsedCalendar.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    
                    if (habit.oneTime && habit.oneTimeDate != null) {
                        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val parsedDate = dateFmt.parse(habit.oneTimeDate)
                        if (parsedDate != null) {
                            val oneTimeCal = Calendar.getInstance().apply { time = parsedDate }
                            set(Calendar.YEAR, oneTimeCal.get(Calendar.YEAR))
                            set(Calendar.MONTH, oneTimeCal.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, oneTimeCal.get(Calendar.DAY_OF_MONTH))
                        }
                        if (before(Calendar.getInstance())) return@forEach // Ya pasó
                    } else {
                        // Buscar el siguiente día válido según repeatDays
                        val validDays = if (habit.repeatDays.isNotEmpty()) habit.repeatDays else listOf(1, 2, 3, 4, 5, 6, 7)
                        var daysAdded = 0
                        
                        while (daysAdded < 7) {
                            val calendarDay = get(Calendar.DAY_OF_WEEK)
                            val appDay = if (calendarDay == Calendar.SUNDAY) 7 else calendarDay - 1
                            
                            if (validDays.contains(appDay) && (daysAdded > 0 || !before(Calendar.getInstance()))) {
                                break // Found the next valid future day
                            }
                            add(Calendar.DATE, 1)
                            daysAdded++
                        }
                    }
                }

                val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
                    putExtra("HABIT_ID", habit.id)
                    putExtra("HABIT_NAME", habit.name)
                    putExtra("TARGET_TIME", timeStr)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    habit.id.hashCode() + timeStr.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("AlarmScheduler", "Alarm scheduled for habit: ${habit.name} at ${calendar.time}")
                } catch (e: SecurityException) {
                    Log.e("AlarmScheduler", "Exact alarm permission denied: ${e.message}")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelHabitAlarm(habit: Habit) {
        val targetTimes = habit.getEffectiveTargetTimes()
        targetTimes.forEach { timeStr ->
            val intent = Intent(context, HabitAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.id.hashCode() + timeStr.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
