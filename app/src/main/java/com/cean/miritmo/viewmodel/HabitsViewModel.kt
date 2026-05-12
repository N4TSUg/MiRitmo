package com.cean.miritmo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cean.miritmo.model.Habit
import com.cean.miritmo.model.HabitRecord
import com.cean.miritmo.repository.AuthRepository
import com.cean.miritmo.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val habitRepository: HabitRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    private val _records = MutableStateFlow<List<HabitRecord>>(emptyList())
    val records: StateFlow<List<HabitRecord>> = _records

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadData() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val habitsResult = habitRepository.getHabitsForUser(userId)
            if (habitsResult.isSuccess) {
                _habits.value = habitsResult.getOrNull() ?: emptyList()
            }
            
            val recordsResult = habitRepository.getRecordsForUser(userId)
            if (recordsResult.isSuccess) {
                _records.value = recordsResult.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun toggleHabitCompletion(habitId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val today = habitRepository.getCurrentDateString()
            if (isCompleted) {
                habitRepository.unmarkHabitCompleted(habitId, today)
            } else {
                habitRepository.markHabitCompleted(habitId, today)
            }
            loadData() // Recargar para actualizar UI
        }
    }

    fun addHabit(name: String, category: String, frequency: String, targetTime: String, onComplete: (Boolean) -> Unit) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val newHabit = Habit(
                id = "",
                userId = userId,
                name = name,
                category = category,
                frequency = frequency,
                targetTime = targetTime,
                createdAt = System.currentTimeMillis()
            )
            val result = habitRepository.createHabit(newHabit)
            if (result.isSuccess) {
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
                loadData()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun deleteHabit(habitId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = habitRepository.deleteHabit(habitId)
            if (result.isSuccess) {
                loadData()
                onSuccess()
            }
        }
    }
}
