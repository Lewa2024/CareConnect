package com.strathmore.CareConnect.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SymptomRepository

    init {
        val db = (application as CareConnectApp).database
        repository = SymptomRepository(db.symptomDao(), db.patientDao())
    }

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    fun loadPatient(caregiverId: Long) {
        viewModelScope.launch {
            repository.getPatientsForCaregiver(caregiverId).collect { patients ->
                _patient.value = patients.firstOrNull()
            }
        }
    }
}