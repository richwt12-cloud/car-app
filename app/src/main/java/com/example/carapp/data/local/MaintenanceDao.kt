package com.example.carapp.data.local

import androidx.room.*
import com.example.carapp.data.model.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_records WHERE carId = :carId ORDER BY serviceDate DESC")
    fun getRecordsForCar(carId: Long): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records ORDER BY serviceDate DESC")
    fun getAllRecords(): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE id = :id")
    suspend fun getRecordById(id: Long): MaintenanceRecord?

    @Query("""
        SELECT * FROM maintenance_records
        WHERE carId = :carId
        AND (nextServiceDate IS NOT NULL OR nextServiceMileage IS NOT NULL)
        ORDER BY nextServiceDate ASC
    """)
    fun getUpcomingForCar(carId: Long): Flow<List<MaintenanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MaintenanceRecord): Long

    @Update
    suspend fun updateRecord(record: MaintenanceRecord)

    @Delete
    suspend fun deleteRecord(record: MaintenanceRecord)

    @Query("SELECT * FROM maintenance_records WHERE carId = :carId AND serviceType = :type ORDER BY serviceDate DESC LIMIT 1")
    suspend fun getLastRecordByType(carId: Long, type: String): MaintenanceRecord?
}
