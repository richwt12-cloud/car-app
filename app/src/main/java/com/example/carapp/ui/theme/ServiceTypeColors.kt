package com.example.carapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.carapp.data.model.ServiceType

fun serviceTypeColor(type: ServiceType): Color = when (type) {
    ServiceType.OIL_CHANGE          -> Color(0xFFF57C00)
    ServiceType.TIRE_ROTATION       -> Color(0xFF1565C0)
    ServiceType.BRAKE_INSPECTION,
    ServiceType.BRAKE_REPLACEMENT   -> Color(0xFFB71C1C)
    ServiceType.AIR_FILTER          -> Color(0xFF00838F)
    ServiceType.CABIN_FILTER        -> Color(0xFF00695C)
    ServiceType.TRANSMISSION_FLUID  -> Color(0xFF6A1B9A)
    ServiceType.BATTERY             -> Color(0xFF2E7D32)
    ServiceType.COOLANT             -> Color(0xFF0277BD)
    ServiceType.SPARK_PLUGS         -> Color(0xFFE65100)
    ServiceType.WIPER_BLADES        -> Color(0xFF4527A0)
    ServiceType.ALIGNMENT,
    ServiceType.TIRE_REPLACEMENT    -> Color(0xFF37474F)
    ServiceType.INSPECTION          -> Color(0xFF33691E)
    ServiceType.OTHER               -> Color(0xFF616161)
}
