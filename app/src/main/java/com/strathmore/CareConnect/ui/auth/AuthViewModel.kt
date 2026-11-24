package com.strathmore.CareConnect.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.User
import com.strathmore.CareConnect.data.repository.AuthRepository
import com.strathmore.CareConnect.data.repository.EmailVerificationSender
import com.strathmore.CareConnect.data.repository.LoginResult
import com.strathmore.CareConnect.data.repository.VerifyEmailResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository

    init {
        val db = (application as CareConnectApp).database
        repository = AuthRepository(
            db.userDao(), db.roleDao(), db.caregiverDao(), db.nurseDao(), db.locationDao(),
            EmailVerificationSender()
        )
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    // Drives the Verify Email screen specifically: code-entry/resend feedback, kept
    // separate from authState so "wrong code" or "code resent" never gets confused
    // with the navigation-driving states below (which other screens also observe).
    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState.asStateFlow()

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
                val result = repository.registerCaregiver(
                    firstName, lastName, email, password, phoneNumber, relationship
                )
                _authState.value = AuthState.PendingVerification(result.userId, result.email)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun registerNurse(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        qualification: String,
        city: String,
        latitude: Double,
        longitude: Double
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || qualification.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        if (city.isBlank()) {
            _authState.value = AuthState.Error("Please share your location so caregivers can find you")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.registerNurse(
                    firstName, lastName, email, password, phoneNumber, qualification, city, latitude, longitude
                )
                _authState.value = AuthState.PendingVerification(result.userId, result.email)
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
            when (val result = repository.login(email, password)) {
                is LoginResult.InvalidCredentials -> {
                    _authState.value = AuthState.Error("Invalid email or password")
                }
                is LoginResult.AccountDeactivated -> {
                    _authState.value = AuthState.Error("This account has been deactivated. Contact an administrator.")
                }
                is LoginResult.EmailNotVerified -> {
                    _authState.value = AuthState.PendingVerification(result.userId, result.email)
                }
                is LoginResult.Success -> {
                    routeVerifiedUser(result.user)
                }
            }
        }
    }

    fun verifyEmail(userId: Long, code: String) {
        if (code.isBlank()) {
            _verificationState.value = VerificationState.Error("Please enter the code")
            return
        }
        viewModelScope.launch {
            _verificationState.value = VerificationState.Sending
            when (val result = repository.verifyEmail(userId, code)) {
                is VerifyEmailResult.Success -> {
                    _verificationState.value = VerificationState.Idle
                    routeVerifiedUser(result.user)
                }
                VerifyEmailResult.InvalidOrExpiredCode -> {
                    _verificationState.value = VerificationState.Error("That code is incorrect or has expired. Try resending it.")
                }
                VerifyEmailResult.AccountNotFound -> {
                    _verificationState.value = VerificationState.Error("Account not found.")
                }
            }
        }
    }

    fun resendVerificationCode(userId: Long) {
        viewModelScope.launch {
            _verificationState.value = VerificationState.Sending
            val result = repository.resendVerificationCode(userId)
            _verificationState.value = if (result.isSuccess) {
                VerificationState.CodeSent
            } else {
                VerificationState.Error("Couldn't send the email. Check your connection and try again.")
            }
        }
    }

    fun resetVerificationState() {
        _verificationState.value = VerificationState.Idle
    }

    /** Shared by login() and verifyEmail(): once an account is confirmed verified
     *  (either it already was, or it just got verified), route to the right home
     *  screen for its role. */
    private suspend fun routeVerifiedUser(user: User) {
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
            "Nurse" -> {
                val nurseId = repository.getNurseId(user.userId)
                if (nurseId == null) {
                    _authState.value = AuthState.Error("Nurse profile not found for this account")
                } else {
                    _authState.value = AuthState.NurseSuccess(nurseId)
                }
            }
            else -> {
                _authState.value = AuthState.Error("This account type isn't supported yet")
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
    data class PendingVerification(val userId: Long, val email: String) : AuthState()
    data class CaregiverSuccess(val caregiverId: Long) : AuthState()
    data class AdminSuccess(val userId: Long) : AuthState()
    data class NurseSuccess(val nurseId: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ResetPasswordState {
    data object Idle : ResetPasswordState()
    data object Loading : ResetPasswordState()
    data object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

sealed class VerificationState {
    data object Idle : VerificationState()
    data object Sending : VerificationState()
    data object CodeSent : VerificationState()
    data class Error(val message: String) : VerificationState()
}