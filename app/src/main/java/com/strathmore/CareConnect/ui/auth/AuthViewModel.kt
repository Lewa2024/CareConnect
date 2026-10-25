package com.strathmore.CareConnect.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository

    init {
        val db = (application as CareConnectApp).database
        repository = AuthRepository(db.userDao(), db.roleDao(), db.caregiverDao())
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        relationship: String
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val userId = repository.registerCaregiver(
                    firstName, lastName, email, password, phoneNumber, relationship
                )
                val caregiverId = repository.getCaregiverId(userId)
                    ?: throw IllegalStateException("Caregiver profile not found after registration")
                _authState.value = AuthState.CaregiverSuccess(caregiverId)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = repository.login(email, password)
            if (user == null) {
                _authState.value = AuthState.Error("Invalid email or password")
                return@launch
            }

            when (repository.getRoleName(user.roleId)) {
                "Administrator" -> {
                    _authState.value = AuthState.AdminSuccess(user.userId)
                }
                "Caregiver" -> {
                    val caregiverId = repository.getCaregiverId(user.userId)
                    if (caregiverId == null) {
                        _authState.value = AuthState.Error("Caregiver profile not found for this account")
                    } else {
                        _authState.value = AuthState.CaregiverSuccess(caregiverId)
                    }
                }
                else -> {
                    _authState.value = AuthState.Error("This account type isn't supported yet")
                }
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        if (email.isBlank() || newPassword.isBlank()) {
            _resetPasswordState.value = ResetPasswordState.Error("Please fill in all fields")
            return
        }
        if (newPassword.length < 6) {
            _resetPasswordState.value = ResetPasswordState.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            try {
                repository.resetPassword(email, newPassword)
                _resetPasswordState.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _resetPasswordState.value = ResetPasswordState.Error(e.message ?: "Failed to reset password")
            }
        }
    }

    fun resetPasswordStateReset() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class CaregiverSuccess(val caregiverId: Long) : AuthState()
    data class AdminSuccess(val userId: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ResetPasswordState {
    data object Idle : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}