package com.cean.miritmo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cean.miritmo.repository.AuthRepository
import com.cean.miritmo.model.User
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    val isUserAuthenticated: Boolean
        get() = repository.getCurrentUserId() != null

    val isDarkMode: StateFlow<Boolean> = repository.isDarkModeFlow
        .map { it ?: false }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    val isNotificationsEnabled: StateFlow<Boolean> = repository.isNotificationsEnabledFlow
        .map { it ?: true }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)

    val notificationSoundUri: StateFlow<String?> = repository.notificationSoundUriFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(isDark)
        }
    }

    fun setNotificationsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(isEnabled)
        }
    }

    fun setNotificationSoundUri(uri: String?) {
        viewModelScope.launch {
            repository.setNotificationSoundUri(uri)
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, pass)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Error de autenticación")
            }
        }
    }

    fun register(name: String, apodo: String? ,email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(name,apodo, email, pass)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Error al registrar")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            }
        }
    }

    fun updateProfile(newName: String, apodo: String?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProfile(newName,apodo)
            if (result.isSuccess) {
                _currentUser.value = _currentUser.value?.copy(name = newName, apodo=apodo?:"")
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun updatePassword(oldPass: String, newPass: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updatePassword(oldPass, newPass)
            onComplete(result.isSuccess)
        }
    }

    fun updateProfilePicture(avatarId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProfilePicture(avatarId)
            if (result.isSuccess) {
                val newUrl = result.getOrNull()
                _currentUser.value = _currentUser.value?.copy(photoUrl = newUrl)
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
