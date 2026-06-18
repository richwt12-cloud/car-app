package com.example.carapp.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CarList : Screen("car_list")
    object AddEditCar : Screen("add_edit_car?carId={carId}") {
        fun routeWithId(carId: Long? = null) =
            if (carId != null) "add_edit_car?carId=$carId" else "add_edit_car?carId=-1"
    }
    object MaintenanceHistory : Screen("maintenance_history/{carId}") {
        fun route(carId: Long) = "maintenance_history/$carId"
    }
    object AddEditMaintenance : Screen("add_edit_maintenance/{carId}?recordId={recordId}") {
        fun route(carId: Long, recordId: Long? = null) =
            if (recordId != null) "add_edit_maintenance/$carId?recordId=$recordId"
            else "add_edit_maintenance/$carId?recordId=-1"
    }
}
