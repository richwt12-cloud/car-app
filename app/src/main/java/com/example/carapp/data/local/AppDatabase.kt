package com.example.carapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.carapp.data.model.Car
import com.example.carapp.data.model.MaintenanceRecord

@Database(
    entities = [Car::class, MaintenanceRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun maintenanceDao(): MaintenanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_maintenance.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
