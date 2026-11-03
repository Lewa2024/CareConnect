package com.strathmore.CareConnect.ui.nurse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strathmore.CareConnect.CareConnectApp
import com.strathmore.CareConnect.data.local.entities.Nurse
import com.strathmore.CareConnect.data.repository.NurseRepository
import com.strathmore.CareConnect.data.repository.NurseWithDistance
import com.strathmore.CareConnect.location.DeviceLocation
import com.strathmore.CareConnect.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NurseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NurseRepository

    init {
        val db = (application as CareConnectApp).database
        repository = NurseRepository(db.nurseDao())
    }

    private val _nurses = MutableStateFlow<List<NurseWithDistance>>(emptyList())
    val nurses: StateFlow<List<NurseWithDistance>> = _nurses.asStateFlow()

    private val _loadState = MutableStateFlow<NurseLoadState>(NurseLoadState.Idle)
    val loadState: StateFlow<NurseLoadState> = _loadState.asStateFlow()

    private val _ownNurseRecord = MutableStateFlow<Nurse?>(null)
    val ownNurseRecord: StateFlow<Nurse?> = _ownNurseRecord.asStateFlow()

    fun loadOwnNurseRecord(nurseId: Long) {
        viewModelScope.launch {
            _ownNurseRecord.value = repository.getNurse(nurseId)
        }
    }

    fun loadNearbyNurses() {
        viewModelScope.launch {
            _loadState.value = NurseLoadState.Loading
            val location: DeviceLocation? = LocationHelper.getCurrentLocation(getApplication())
            if (location == null) {
                _loadState.value = NurseLoadState.LocationUnavailable
                return@launch
            }
            repository.getAvailableNursesSortedByDistance(location.latitude, location.longitude)
                .collect { list ->
                    _nurses.value = list
                    _loadState.value = NurseLoadState.Loaded
                }
        }
    }

    fun setAvailable(nurseId: Long, available: Boolean) {
        viewModelScope.launch {
            repository.setAvailable(nurseId, available)
            _ownNurseRecord.value = repository.getNurse(nurseId)
        }
    }
}

sealed class NurseLoadState {
    data object Idle : NurseLoadState()
    data object Loading : NurseLoadState()
    data object LocationUnavailable : NurseLoadState()
    data object Loaded : NurseLoadState()
}