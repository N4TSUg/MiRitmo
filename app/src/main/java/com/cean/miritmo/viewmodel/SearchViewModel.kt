package com.cean.miritmo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cean.miritmo.model.Habit
import com.cean.miritmo.model.User
import com.cean.miritmo.repository.AuthRepository
import com.cean.miritmo.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _userProfileHabits = MutableStateFlow<List<Habit>>(emptyList())
    val userProfileHabits: StateFlow<List<Habit>> = _userProfileHabits.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val result = authRepository.searchUsers(query)
            if (result.isSuccess) {
                _searchResults.value = result.getOrNull() ?: emptyList()
            }
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            // Cargar el perfil (necesitaríamos un get user by id, si no existe lo buscamos de los resultados de búsqueda)
            val user = _searchResults.value.find { it.id == userId }
            if (user != null) {
                _userProfile.value = user
            }

            // Cargar hábitos
            val habitsResult = habitRepository.getHabitsForUser(userId)
            if (habitsResult.isSuccess) {
                val habits = habitsResult.getOrNull() ?: emptyList()
                // Filtrar solo los hábitos activos, no eliminados y públicos
                val publicHabits = habits.filter { it.isActive && !it.isDeleted && !it.isPrivate }
                _userProfileHabits.value = publicHabits
            }
        }
    }
}
