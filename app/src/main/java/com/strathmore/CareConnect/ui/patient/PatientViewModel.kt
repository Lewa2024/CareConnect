package com.strathmore.CareConnect.ui.patient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatientViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatientRepository

    init {
        val db = (application as CareConnectApp).database
        repository = PatientRepository(db.patientDao())
    }

    private val _saveState = MutableStateFlow<PatientSaveState>(PatientSaveState.Idle)
    val saveState: StateFlow<PatientSaveState> = _saveState.asStateFlow()

    private val _editingPatient = MutableStateFlow<Patient?>(null)
    val editingPatient: StateFlow<Patient?> = _editingPatient.asStateFlow()

    private val _editState = MutableStateFlow<EditState>(EditState.Idle)
    val editState: StateFlow<EditState> = _editState.asStateFlow()

    fun addPatient(
        caregiverId: Long,
        name: String,
        dateOfBirthMillis: Long,
        medicalHistory: String,
        medication: String
    ) {
        if (name.isBlank()) {
            _saveState.value = PatientSaveState.Error("Please enter the patient's name")
            return
        }
        viewModelScope.launch {
            _saveState.value = PatientSaveState.Saving
            try {
                val patientId = repository.addPatient(
                    caregiverId, name, dateOfBirthMillis, medicalHistory, medication
                )
                _saveState.value = PatientSaveState.Saved(patientId)
            } catch (e: Exception) {
                _saveState.value = PatientSaveState.Error(e.message ?: "Failed to save patient")
            }
        }
    }

    fun loadPatientForEdit(patientId: Long) {
        viewModelScope.launch {
            _editingPatient.value = repository.getPatient(patientId)
        }
    }

    fun updatePatientName(patientId: Long, newName: String) {
        if (newName.isBlank()) {
            _editState.value = EditState.Error("Name can't be empty")
            return
        }
        viewModelScope.launch {
            _editState.value = EditState.Saving
            try {
                repository.updatePatientName(patientId, newName)
                _editState.value = EditState.Saved
            } catch (e: Exception) {
                _editState.value = EditState.Error(e.message ?: "Failed to update name")
            }
        }
    }

    fun resetState() {
        _saveState.value = PatientSaveState.Idle
    }

    fun resetEditState() {
        _editState.value = EditState.Idle
    }
}

sealed class PatientSaveState {
    data object Idle : PatientSaveState()
    data object Saving : PatientSaveState()
    data class Saved(val patientId: Long) : PatientSaveState()
    data class Error(val message: String) : PatientSaveState()
}

sealed class EditState {
    data object Idle : EditState()
    data object Saving : EditState()
    data object Saved : EditState()
    data class Error(val message: String) : EditState()
}