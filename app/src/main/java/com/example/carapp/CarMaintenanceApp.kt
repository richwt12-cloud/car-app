package com.example.carapp

import android.app.Application
import com.example.carapp.data.local.AppDatabase
import com.example.carapp.repository.MaintenanceRepository

class CarMaintenanceApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        MaintenanceRepository(database.carDao(), database.maintenanceDao())
    }
}
