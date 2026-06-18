package com.example.carapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carapp.data.model.Car
import com.example.carapp.repository.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CarViewModel(private val repository: MaintenanceRepository) : ViewModel() {

    val cars: StateFlow<List<Car>> = repository.allCars.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _selectedCarId = MutableStateFlow<Long?>(null)
    val selectedCarId: StateFlow<Long?> = _selectedCarId.asStateFlow()

    fun selectCar(carId: Long) {
        _selectedCarId.value = carId
    }

    fun addCar(car: Car, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertCar(car)
            _selectedCarId.value = id
            onResult(id)
        }
    }

    fun updateCar(car: Car) {
        viewModelScope.launch { repository.updateCar(car) }
    }

    fun deleteCar(car: Car) {
        viewModelScope.launch {
            repository.deleteCar(car)
            if (_selectedCarId.value == car.id) {
                _selectedCarId.value = null
            }
        }
    }

    class Factory(private val repository: MaintenanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CarViewModel(repository) as T
    }
}
