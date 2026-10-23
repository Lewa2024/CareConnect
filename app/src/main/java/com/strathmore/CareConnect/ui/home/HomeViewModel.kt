package com.strathmore.CareConnect.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.local.entities.Symptom
import com.strathmore.CareConnect.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SymptomRepository
    private val notificationDao = (application as CareConnectApp).database.notificationDao()

    init {
        val db = (application as CareConnectApp).database
        repository = SymptomRepository(db.symptomDao(), db.patientDao(), db.ruleThresholdDao(), db.notificationDao())
    }

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _latestEntry = MutableStateFlow<Symptom?>(null)
    val latestEntry: StateFlow<Symptom?> = _latestEntry.asStateFlow()

    private val _latestAlert = MutableStateFlow<Notification?>(null)
    val latestAlert: StateFlow<Notification?> = _latestAlert.asStateFlow()

    fun loadPatient(caregiverId: Long) {
        viewModelScope.launch {
            repository.getPatientsForCaregiver(caregiverId).collect { patients ->
                _patient.value = patients.firstOrNull()
                _patient.value?.let {
                    loadLatestEntry(it.patientId)
                    loadLatestAlert(it.patientId)
                }
            }
        }
    }

    private fun loadLatestEntry(patientId: Long) {
        viewModelScope.launch {
            _latestEntry.value = repository.getLatestEntry(patientId)
        }
    }

    private fun loadLatestAlert(patientId: Long) {
        viewModelScope.launch {
            _latestAlert.value = notificationDao.getMostRecent(patientId)
        }
    }
}