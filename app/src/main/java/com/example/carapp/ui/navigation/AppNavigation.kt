package com.example.carapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object CarList : Screen("car_list")
    object AddEditCar : Screen("add_edit_car?carId={carId}") {
        fun routeWithId(carId: Long? = null) =
            if (carId != null) "add_edit_car?carId=$carId" else "add_edit_car?carId=-1"
    }
    object AddEditMaintenance : Screen("add_edit_maintenance/{carId}?recordId={recordId}") {
        fun route(carId: Long, recordId: Long? = null) =
            if (recordId != null) "add_edit_maintenance/$carId?recordId=$recordId"
            else "add_edit_maintenance/$carId?recordId=-1"
    }
}

val bottomNavRoutes = setOf(Screen.Home.route, Screen.History.route, Screen.CarList.route)

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home, "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object History : BottomNavItem(Screen.History, "History", Icons.Outlined.History, Icons.Filled.History)
    object Cars : BottomNavItem(Screen.CarList, "My Cars", Icons.Outlined.DirectionsCar, Icons.Filled.DirectionsCar)
}

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.History, BottomNavItem.Cars)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
