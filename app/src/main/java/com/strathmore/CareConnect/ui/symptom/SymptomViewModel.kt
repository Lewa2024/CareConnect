package com.strathmore.CareConnect.ui.symptom

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Symptom
import com.strathmore.CareConnect.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SymptomHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SymptomRepository

    init {
        val db = (application as CareConnectApp).database
        repository = SymptomRepository(db.symptomDao(), db.patientDao())
    }

    private val _history = MutableStateFlow<List<Symptom>>(emptyList())
    val history: StateFlow<List<Symptom>> = _history.asStateFlow()

    fun loadHistory(patientId: Long) {
        viewModelScope.launch {
            repository.getHistory(patientId).collect { list ->
                _history.value = list
            }
        }
    }
}