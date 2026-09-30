package com.strathmore.CareConnect.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.User
import com.strathmore.CareConnect.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository

    init {
        val db = (application as CareConnectApp).database
        repository = AuthRepository(db.userDao(), db.roleDao(), db.caregiverDao())
    }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun loadUser(caregiverId: Long) {
        viewModelScope.launch {
            _user.value = repository.getUserForCaregiver(caregiverId)
        }
    }
}