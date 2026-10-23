package com.strathmore.CareConnect.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationDao = (application as CareConnectApp).database.notificationDao()

    private val _alerts = MutableStateFlow<List<Notification>>(emptyList())
    val alerts: StateFlow<List<Notification>> = _alerts.asStateFlow()

    fun loadAlerts(patientId: Long) {
        viewModelScope.launch {
            notificationDao.getForPatient(patientId).collect { list ->
                _alerts.value = list
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationDao.markAsRead(notificationId)
        }
    }
}