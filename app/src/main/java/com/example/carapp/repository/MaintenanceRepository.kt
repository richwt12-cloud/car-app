package com.example.carapp.repository

import com.example.carapp.data.local.CarDao
import com.example.carapp.data.local.MaintenanceDao
import com.example.carapp.data.model.Car
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(
    private val carDao: CarDao,
    private val maintenanceDao: MaintenanceDao
) {
    val allCars: Flow<List<Car>> = carDao.getAllCars()

    fun getRecordsForCar(carId: Long): Flow<List<MaintenanceRecord>> =
        maintenanceDao.getRecordsForCar(carId)

    fun getUpcomingForCar(carId: Long): Flow<List<MaintenanceRecord>> =
        maintenanceDao.getUpcomingForCar(carId)

    suspend fun getCarById(id: Long): Car? = carDao.getCarById(id)

    suspend fun insertCar(car: Car): Long = carDao.insertCar(car)

    suspend fun updateCar(car: Car) = carDao.updateCar(car)

    suspend fun deleteCar(car: Car) = carDao.deleteCar(car)

    suspend fun insertRecord(record: MaintenanceRecord): Long =
        maintenanceDao.insertRecord(record)

    suspend fun updateRecord(record: MaintenanceRecord) =
        maintenanceDao.updateRecord(record)

    suspend fun deleteRecord(record: MaintenanceRecord) =
        maintenanceDao.deleteRecord(record)

    suspend fun getRecordById(id: Long): MaintenanceRecord? =
        maintenanceDao.getRecordById(id)

    suspend fun getLastRecordByType(carId: Long, type: ServiceType): MaintenanceRecord? =
        maintenanceDao.getLastRecordByType(carId, type.name)
}
