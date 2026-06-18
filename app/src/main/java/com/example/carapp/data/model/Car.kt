package com.example.carapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val make: String,
    val model: String,
    val year: Int,
    val licensePlate: String = "",
    val vin: String = "",
    val currentMileage: Int = 0,
    val color: String = ""
)
