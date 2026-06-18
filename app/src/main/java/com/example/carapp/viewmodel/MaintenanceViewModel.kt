package com.example.carapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
import com.example.carapp.repository.MaintenanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MaintenanceViewModel(private val repository: MaintenanceRepository) : ViewModel() {

    private val _currentCarId = MutableStateFlow<Long?>(null)

    val records: StateFlow<List<MaintenanceRecord>> = _currentCarId.flatMapLatest { carId ->
        if (carId != null) repository.getRecordsForCar(carId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val upcomingRecords: StateFlow<List<MaintenanceRecord>> = _currentCarId.flatMapLatest { carId ->
        if (carId != null) repository.getUpcomingForCar(carId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCarId(carId: Long?) {
        _currentCarId.value = carId
    }

    fun addRecord(record: MaintenanceRecord, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertRecord(record)
            onResult(id)
        }
    }

    fun updateRecord(record: MaintenanceRecord) {
        viewModelScope.launch { repository.updateRecord(record) }
    }

    fun deleteRecord(record: MaintenanceRecord) {
        viewModelScope.launch { repository.deleteRecord(record) }
    }

    suspend fun getRecordById(id: Long): MaintenanceRecord? = repository.getRecordById(id)

    suspend fun getLastRecordByType(carId: Long, type: ServiceType): MaintenanceRecord? =
        repository.getLastRecordByType(carId, type)

    class Factory(private val repository: MaintenanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaintenanceViewModel(repository) as T
    }
}
