package com.example.carapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.carapp.data.model.ServiceType

@Composable
fun ServiceTypeIcon(
    serviceType: ServiceType,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val icon = when (serviceType) {
        ServiceType.OIL_CHANGE                          -> Icons.Default.WaterDrop
        ServiceType.TIRE_ROTATION,
        ServiceType.TIRE_REPLACEMENT,
        ServiceType.ALIGNMENT                           -> Icons.Default.TireRepair
        ServiceType.BRAKE_INSPECTION,
        ServiceType.BRAKE_REPLACEMENT                   -> Icons.Default.PanTool
        ServiceType.BATTERY                             -> Icons.Default.BatteryChargingFull
        ServiceType.AIR_FILTER, ServiceType.CABIN_FILTER -> Icons.Default.Air
        ServiceType.INSPECTION                          -> Icons.Default.FactCheck
        ServiceType.WIPER_BLADES                        -> Icons.Default.Visibility
        ServiceType.COOLANT                             -> Icons.Default.Opacity
        ServiceType.TRANSMISSION_FLUID                  -> Icons.Default.Settings
        ServiceType.SPARK_PLUGS                         -> Icons.Default.ElectricBolt
        else                                            -> Icons.Default.Build
    }
    Icon(icon, contentDescription = serviceType.displayName, modifier = modifier, tint = tint)
}
