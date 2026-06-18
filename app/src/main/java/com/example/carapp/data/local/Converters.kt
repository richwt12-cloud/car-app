package com.example.carapp.data.local

import androidx.room.TypeConverter
import com.example.carapp.data.model.ServiceType

class Converters {
    @TypeConverter
    fun fromServiceType(value: ServiceType): String = value.name

    @TypeConverter
    fun toServiceType(value: String): ServiceType = ServiceType.valueOf(value)
}
