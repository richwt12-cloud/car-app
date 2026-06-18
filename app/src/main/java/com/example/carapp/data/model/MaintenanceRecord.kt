package com.example.carapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ServiceType(val displayName: String) {
    OIL_CHANGE("Oil Change"),
    TIRE_ROTATION("Tire Rotation"),
    BRAKE_INSPECTION("Brake Inspection"),
    BRAKE_REPLACEMENT("Brake Replacement"),
    AIR_FILTER("Air Filter"),
    CABIN_FILTER("Cabin Filter"),
    TRANSMISSION_FLUID("Transmission Fluid"),
    BATTERY("Battery"),
    COOLANT("Coolant Flush"),
    SPARK_PLUGS("Spark Plugs"),
    WIPER_BLADES("Wiper Blades"),
    ALIGNMENT("Wheel Alignment"),
    TIRE_REPLACEMENT("Tire Replacement"),
    INSPECTION("Vehicle Inspection"),
    OTHER("Other")
}

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["carId"])]
)
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val carId: Long,
    val serviceType: ServiceType,
    val customServiceName: String = "",
    val serviceDate: Long,
    val mileageAtService: Int,
    val cost: Double = 0.0,
    val shop: String = "",
    val notes: String = "",
    val nextServiceDate: Long? = null,
    val nextServiceMileage: Int? = null
) {
    val displayServiceName: String
        get() = if (serviceType == ServiceType.OTHER && customServiceName.isNotBlank()) {
            customServiceName
        } else {
            serviceType.displayName
        }
}
