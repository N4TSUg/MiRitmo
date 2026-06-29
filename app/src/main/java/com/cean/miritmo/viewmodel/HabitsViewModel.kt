package com.cean.miritmo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cean.miritmo.model.Habit
import com.cean.miritmo.model.HabitRecord
import com.cean.miritmo.model.Routine
import com.cean.miritmo.repository.AuthRepository
import com.cean.miritmo.repository.HabitRepository
import com.cean.miritmo.util.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class TimerState(
    val habitId: String,
    val secondsRemaining: Int,
    val isRunning: Boolean,
    val totalSeconds: Int
)

class HabitsViewModel(
    application: Application,
    private val habitRepository: HabitRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _timers = MutableStateFlow<Map<String, TimerState>>(emptyMap())
    val timers: StateFlow<Map<String, TimerState>> = _timers

    fun getOrCreateTimer(habitId: String, durationMinutes: Int): TimerState {
        val currentMap = _timers.value
        if (currentMap.containsKey(habitId)) {
            return currentMap[habitId]!!
        }
        val totalSeconds = durationMinutes * 60
        val newTimer = TimerState(habitId, totalSeconds, false, totalSeconds)
        _timers.value = currentMap + (habitId to newTimer)
        return newTimer
    }

    fun toggleTimer(habitId: String) {
        val timer = _timers.value[habitId] ?: return
        val willRun = !timer.isRunning
        _timers.value = _timers.value + (habitId to timer.copy(isRunning = willRun))
        
        if (willRun) {
            viewModelScope.launch {
                while (_timers.value[habitId]?.isRunning == true && (_timers.value[habitId]?.secondsRemaining ?: 0) > 0) {
                    delay(1000L)
                    val current = _timers.value[habitId] ?: break
                    if (current.isRunning && current.secondsRemaining > 0) {
                        val newSeconds = current.secondsRemaining - 1
                        _timers.value = _timers.value + (habitId to current.copy(
                            secondsRemaining = newSeconds,
                            isRunning = newSeconds > 0
                        ))
                    } else {
                        break
                    }
                }
            }
        }
    }

    fun loadData() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val habitsResult = habitRepository.getHabitsForUser(userId)
            if (habitsResult.isSuccess) {
                _habits.value = habitsResult.getOrNull() ?: emptyList()
            }
            
            val routinesResult = habitRepository.getRoutinesForUser(userId)
            if (routinesResult.isSuccess) {
                _routines.value = routinesResult.getOrNull() ?: emptyList()
            }
            
            // Programar las alarmas para todos los hábitos cargados
            _habits.value.forEach { alarmScheduler.scheduleHabitAlarm(it) }
            
            _isLoading.value = false
        }
    }

    fun toggleHabitCompletion(habitId: String, isFullyCompleted: Boolean, dateStr: String) {
        viewModelScope.launch {
            val habit = getHabitById(habitId) ?: return@launch
            
            val totalTargets = maxOf(1, habit.getEffectiveTargetTimes().size)
            val currentCompletions = habit.completionsByDate[dateStr] ?: 0
            
            val newHabit = if (isFullyCompleted) {
                // If it was fully completed, user wants to uncheck it (decrement or reset)
                val newCompletions = maxOf(0, currentCompletions - 1)
                val newMap = habit.completionsByDate + (dateStr to newCompletions)
                
                val newStreak = maxOf(0, habit.currentStreak - 1)
                val newLastCompleted = if (habit.lastCompletedDate == dateStr) null else habit.lastCompletedDate
                
                habit.copy(completionsByDate = newMap, lastCompletedDate = newLastCompleted, currentStreak = newStreak)
            } else {
                // User checks it, increment completion
                val newCompletions = currentCompletions + 1
                val newMap = habit.completionsByDate + (dateStr to newCompletions)
                val reachesGoal = newCompletions >= totalTargets
                
                val newStreak = if (reachesGoal) habit.currentStreak + 1 else habit.currentStreak
                val newLastCompleted = if (reachesGoal) dateStr else habit.lastCompletedDate
                
                habit.copy(completionsByDate = newMap, lastCompletedDate = newLastCompleted, currentStreak = newStreak)
            }
            
            val result = habitRepository.updateHabit(newHabit)
            if (result.isSuccess) {
                loadData()
            }
        }
    }

    var habitToCopy: Habit? = null

    fun addHabit(name: String, category: String, frequency: String, repeatDays: List<Int>, targetTimes: List<String>, durationMinutes: Int?, oneTime: Boolean = false, oneTimeDate: String? = null, isPrivate: Boolean = false, routineId: String? = null, onComplete: (Boolean) -> Unit) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val newHabit = Habit(
                id = "",
                userId = userId,
                name = name,
                category = category,
                frequency = frequency,
                repeatDays = repeatDays,
                oneTime = oneTime,
                oneTimeDate = oneTimeDate,
                targetTime = if (targetTimes.isNotEmpty()) targetTimes[0] else null, // Fallback for old versions
                targetTimes = targetTimes,
                durationMinutes = durationMinutes,
                createdAt = System.currentTimeMillis(),
                isPrivate = isPrivate,
                routineId = routineId
            )
            val result = habitRepository.createHabit(newHabit)
            if (result.isSuccess) {
                val createdHabitId = result.getOrNull()
                if (createdHabitId != null && routineId != null) {
                    val routine = _routines.value.find { it.id == routineId }
                    if (routine != null) {
                        val newHabitIds = routine.habitIds.toMutableList().apply { add(createdHabitId) }
                        habitRepository.updateRoutine(routine.copy(habitIds = newHabitIds))
                        habitRepository.addHabitsToRoutine(routineId, listOf(createdHabitId))
                    }
                }
                loadData()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun getHabitById(habitId: String): Habit? {
        return _habits.value.find { it.id == habitId }
    }

    fun updateHabit(habit: Habit, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = habitRepository.updateHabit(habit)
            if (result.isSuccess) {
                if (habit.isActive && !habit.isDeleted) {
                    alarmScheduler.scheduleHabitAlarm(habit)
                } else {
                    alarmScheduler.cancelHabitAlarm(habit)
                }
                loadData()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun deleteHabit(habitId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val habit = getHabitById(habitId) ?: return@launch
            val result = habitRepository.updateHabit(habit.copy(isDeleted = true))
            if (result.isSuccess) {
                alarmScheduler.cancelHabitAlarm(habit)
                loadData()
                onSuccess()
            }
        }
    }

    fun permanentlyDeleteHabit(habitId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val habit = getHabitById(habitId)
            val result = habitRepository.deleteHabit(habitId)
            if (result.isSuccess) {
                if (habit != null) {
                    alarmScheduler.cancelHabitAlarm(habit)
                    if (habit.routineId != null) {
                        val routine = _routines.value.find { it.id == habit.routineId }
                        if (routine != null) {
                            val newHabitIds = routine.habitIds - habit.id
                            habitRepository.updateRoutine(routine.copy(habitIds = newHabitIds))
                        }
                    }
                }
                loadData()
                onSuccess()
            }
        }
    }

    fun toggleHabitActiveStatus(habitId: String) {
        viewModelScope.launch {
            val habit = getHabitById(habitId) ?: return@launch
            val newHabit = habit.copy(isActive = !habit.isActive)
            val result = habitRepository.updateHabit(newHabit)
            if (result.isSuccess) {
                if (newHabit.isActive && !newHabit.isDeleted) {
                    alarmScheduler.scheduleHabitAlarm(newHabit)
                } else {
                    alarmScheduler.cancelHabitAlarm(newHabit)
                }
                loadData()
            }
        }
    }

    fun restoreHabit(habitId: String) {
        viewModelScope.launch {
            val habit = getHabitById(habitId) ?: return@launch
            val newHabit = habit.copy(isDeleted = false)
            val result = habitRepository.updateHabit(newHabit)
            if (result.isSuccess) {
                if (newHabit.isActive) {
                    alarmScheduler.scheduleHabitAlarm(newHabit)
                }
                loadData()
            }
        }
    }

    fun copyHabitToMyProfile(habit: Habit, onSuccess: () -> Unit) {
        val currentUserId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val newHabit = habit.copy(
                id = "",
                userId = currentUserId,
                routineId = null,
                createdAt = System.currentTimeMillis(),
                completionsByDate = emptyMap(),
                lastCompletedDate = null,
                currentStreak = 0
            )
            val result = habitRepository.createHabit(newHabit)
            if (result.isSuccess) {
                loadData()
                onSuccess()
            }
        }
    }

    fun copyRoutineToMyProfile(routine: Routine, routineHabits: List<Habit>, onSuccess: () -> Unit) {
        val currentUserId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val newRoutine = routine.copy(
                id = "",
                userId = currentUserId,
                habitIds = emptyList(), // will be populated
                createdAt = System.currentTimeMillis()
            )
            val routineResult = habitRepository.createRoutine(newRoutine)
            if (routineResult.isSuccess) {
                val newRoutineId = routineResult.getOrNull() ?: ""
                val newHabitIds = mutableListOf<String>()
                
                for (habit in routineHabits) {
                    val newHabit = habit.copy(
                        id = "",
                        userId = currentUserId,
                        routineId = newRoutineId,
                        createdAt = System.currentTimeMillis(),
                        completionsByDate = emptyMap(),
                        lastCompletedDate = null,
                        currentStreak = 0
                    )
                    val habitResult = habitRepository.createHabit(newHabit)
                    if (habitResult.isSuccess) {
                        newHabitIds.add(habitResult.getOrNull()!!)
                    }
                }
                
                val updatedRoutine = newRoutine.copy(id = newRoutineId, habitIds = newHabitIds)
                habitRepository.updateRoutine(updatedRoutine)
                
                loadData()
                onSuccess()
            }
        }
    }


    fun addRoutine(name: String, description: String, habitIds: List<String>, isPrivate: Boolean, onComplete: (Boolean) -> Unit) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val newRoutine = Routine(
                id = "",
                userId = userId,
                name = name,
                description = description,
                habitIds = habitIds,
                createdAt = System.currentTimeMillis(),
                isPrivate = isPrivate
            )
            val result = habitRepository.createRoutine(newRoutine)
            if (result.isSuccess) {
                val newRoutineId = result.getOrNull()
                if (newRoutineId != null && habitIds.isNotEmpty()) {
                    habitRepository.addHabitsToRoutine(newRoutineId, habitIds)
                }
                loadData()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun updateRoutine(routine: Routine, newHabitIds: List<String>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val removedHabitIds = routine.habitIds.filter { it !in newHabitIds }
            val updatedRoutine = routine.copy(habitIds = newHabitIds)
            val result = habitRepository.updateRoutine(updatedRoutine)
            if (result.isSuccess) {
                if (newHabitIds.isNotEmpty()) {
                    habitRepository.addHabitsToRoutine(routine.id, newHabitIds)
                }
                if (removedHabitIds.isNotEmpty()) {
                    habitRepository.removeHabitsFromRoutine(removedHabitIds)
                }
                loadData()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun deleteRoutine(routineId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = habitRepository.deleteRoutine(routineId)
            if (result.isSuccess) {
                loadData()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun permanentlyDeleteRoutine(routineId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val routine = _routines.value.find { it.id == routineId }
            val result = habitRepository.permanentlyDeleteRoutine(routineId)
            if (result.isSuccess) {
                if (routine != null && routine.habitIds.isNotEmpty()) {
                    habitRepository.removeHabitsFromRoutine(routine.habitIds)
                }
                loadData()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun restoreRoutine(routineId: String) {
        viewModelScope.launch {
            val routine = _routines.value.find { it.id == routineId } ?: return@launch
            val updatedRoutine = routine.copy(isDeleted = false)
            val result = habitRepository.updateRoutine(updatedRoutine)
            if (result.isSuccess) {
                loadData()
            }
        }
    }

    fun toggleRoutineActiveStatus(routineId: String) {
        viewModelScope.launch {
            val routine = _routines.value.find { it.id == routineId } ?: return@launch
            val updatedRoutine = routine.copy(isActive = !routine.isActive)
            val result = habitRepository.updateRoutine(updatedRoutine)
            if (result.isSuccess) {
                loadData()
            }
        }
    }
}
