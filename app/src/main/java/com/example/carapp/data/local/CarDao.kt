package com.example.carapp.data.local

import androidx.room.*
import com.example.carapp.data.model.Car
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY make, model")
    fun getAllCars(): Flow<List<Car>>

    @Query("SELECT * FROM cars WHERE id = :id")
    suspend fun getCarById(id: Long): Car?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car): Long

    @Update
    suspend fun updateCar(car: Car)

    @Delete
    suspend fun deleteCar(car: Car)
}
