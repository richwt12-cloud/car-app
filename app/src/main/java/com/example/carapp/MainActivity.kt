package com.example.carapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carapp.ui.navigation.Screen
import com.example.carapp.ui.screens.*
import com.example.carapp.ui.theme.CarMaintenanceTheme
import com.example.carapp.viewmodel.CarViewModel
import com.example.carapp.viewmodel.MaintenanceViewModel

class MainActivity : ComponentActivity() {

    private val carViewModel: CarViewModel by viewModels {
        CarViewModel.Factory((application as CarMaintenanceApp).repository)
    }

    private val maintenanceViewModel: MaintenanceViewModel by viewModels {
        MaintenanceViewModel.Factory((application as CarMaintenanceApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarMaintenanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.Home.route) {

                        composable(Screen.Home.route) {
                            HomeScreen(
                                carViewModel = carViewModel,
                                maintenanceViewModel = maintenanceViewModel,
                                onNavigateToCarList = { navController.navigate(Screen.CarList.route) },
                                onNavigateToHistory = { carId ->
                                    navController.navigate(Screen.MaintenanceHistory.route(carId))
                                },
                                onNavigateToAddMaintenance = { carId ->
                                    navController.navigate(Screen.AddEditMaintenance.route(carId))
                                },
                                onNavigateToAddCar = {
                                    navController.navigate(Screen.AddEditCar.routeWithId())
                                }
                            )
                        }

                        composable(Screen.CarList.route) {
                            CarListScreen(
                                carViewModel = carViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddCar = {
                                    navController.navigate(Screen.AddEditCar.routeWithId())
                                },
                                onNavigateToEditCar = { carId ->
                                    navController.navigate(Screen.AddEditCar.routeWithId(carId))
                                },
                                onNavigateToHistory = { carId ->
                                    navController.navigate(Screen.MaintenanceHistory.route(carId))
                                }
                            )
                        }

                        composable(
                            route = Screen.AddEditCar.route,
                            arguments = listOf(navArgument("carId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            })
                        ) { backStackEntry ->
                            val carId = backStackEntry.arguments?.getLong("carId")
                            AddEditCarScreen(
                                carId = if (carId != null && carId > 0) carId else null,
                                carViewModel = carViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.MaintenanceHistory.route,
                            arguments = listOf(navArgument("carId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val carId = backStackEntry.arguments!!.getLong("carId")
                            MaintenanceHistoryScreen(
                                carId = carId,
                                carViewModel = carViewModel,
                                maintenanceViewModel = maintenanceViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddMaintenance = { id ->
                                    navController.navigate(Screen.AddEditMaintenance.route(id))
                                },
                                onNavigateToEditMaintenance = { cid, rid ->
                                    navController.navigate(Screen.AddEditMaintenance.route(cid, rid))
                                }
                            )
                        }

                        composable(
                            route = Screen.AddEditMaintenance.route,
                            arguments = listOf(
                                navArgument("carId") { type = NavType.LongType },
                                navArgument("recordId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val carId = backStackEntry.arguments!!.getLong("carId")
                            val recordId = backStackEntry.arguments?.getLong("recordId")
                            AddEditMaintenanceScreen(
                                carId = carId,
                                recordId = if (recordId != null && recordId > 0) recordId else null,
                                carViewModel = carViewModel,
                                maintenanceViewModel = maintenanceViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
