package com.cean.miritmo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cean.miritmo.model.Habit
import com.cean.miritmo.model.Routine
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

    private val _userProfileRoutines = MutableStateFlow<List<Routine>>(emptyList())
    val userProfileRoutines: StateFlow<List<Routine>> = _userProfileRoutines.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _followersList = MutableStateFlow<List<User>>(emptyList())
    val followersList: StateFlow<List<User>> = _followersList.asStateFlow()

    private val _isProcessingFollow = MutableStateFlow(false)
    val isProcessingFollow: StateFlow<Boolean> = _isProcessingFollow.asStateFlow()

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
            // Cargar el perfil real para tener la información más fresca de seguidores
            val usersResult = authRepository.getUsersByIds(listOf(userId))
            val freshUser = usersResult.getOrNull()?.firstOrNull()
            if (freshUser != null) {
                _userProfile.value = freshUser
            } else {
                val user = _searchResults.value.find { it.id == userId }
                if (user != null) {
                    _userProfile.value = user
                }
            }

            // Chequear si lo seguimos
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId != null && _userProfile.value != null) {
                _isFollowing.value = _userProfile.value!!.followers.contains(currentUserId)
            }

            // Cargar perfiles de los seguidores
            val followersIds = _userProfile.value?.followers ?: emptyList()
            if (followersIds.isNotEmpty()) {
                val followersResult = authRepository.getUsersByIds(followersIds)
                if (followersResult.isSuccess) {
                    _followersList.value = followersResult.getOrNull() ?: emptyList()
                }
            } else {
                _followersList.value = emptyList()
            }

            // Cargar hábitos y rutinas
            val habitsResult = habitRepository.getHabitsForUser(userId)
            if (habitsResult.isSuccess) {
                val habits = habitsResult.getOrNull() ?: emptyList()
                // Filtrar solo los hábitos activos, no eliminados y públicos
                val publicHabits = habits.filter { it.isActive && !it.isDeleted && !it.isPrivate }
                _userProfileHabits.value = publicHabits
            }

            val routinesResult = habitRepository.getRoutinesForUser(userId)
            if (routinesResult.isSuccess) {
                val routines = routinesResult.getOrNull() ?: emptyList()
                // Filtrar solo rutinas activas, no eliminadas y públicas
                val publicRoutines = routines.filter { it.isActive && !it.isDeleted && !it.isPrivate }
                _userProfileRoutines.value = publicRoutines
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            if (_isProcessingFollow.value) return@launch
            _isProcessingFollow.value = true
            
            val currentlyFollowing = _isFollowing.value
            val result = if (currentlyFollowing) {
                authRepository.unfollowUser(targetUserId)
            } else {
                authRepository.followUser(targetUserId)
            }
            
            if (result.isSuccess) {
                // Recargar el perfil para obtener la cuenta fresca de seguidores y estado
                loadUserProfile(targetUserId)
            }
            
            _isProcessingFollow.value = false
        }
    }
}
