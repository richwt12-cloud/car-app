package com.example.carapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.carapp.ui.navigation.*
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
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in bottomNavRoutes) {
                            AppBottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 4 } },
                        exitTransition = { fadeOut(tween(150)) },
                        popEnterTransition = { fadeIn(tween(200)) },
                        popExitTransition = { fadeOut(tween(150)) + slideOutHorizontally(tween(200)) { it / 4 } }
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                carViewModel = carViewModel,
                                maintenanceViewModel = maintenanceViewModel,
                                onNavigateToAddMaintenance = { carId ->
                                    navController.navigate(Screen.AddEditMaintenance.route(carId))
                                },
                                onNavigateToAddCar = {
                                    navController.navigate(Screen.AddEditCar.routeWithId())
                                },
                                onNavigateToHistory = {
                                    navController.navigate(Screen.History.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        composable(Screen.History.route) {
                            HistoryTabScreen(
                                carViewModel = carViewModel,
                                maintenanceViewModel = maintenanceViewModel,
                                onNavigateToAddMaintenance = { carId ->
                                    navController.navigate(Screen.AddEditMaintenance.route(carId))
                                },
                                onNavigateToEditMaintenance = { carId, recordId ->
                                    navController.navigate(Screen.AddEditMaintenance.route(carId, recordId))
                                },
                                onNavigateToAddCar = {
                                    navController.navigate(Screen.AddEditCar.routeWithId())
                                }
                            )
                        }

                        composable(Screen.CarList.route) {
                            CarListScreen(
                                carViewModel = carViewModel,
                                onNavigateToAddCar = {
                                    navController.navigate(Screen.AddEditCar.routeWithId())
                                },
                                onNavigateToEditCar = { carId ->
                                    navController.navigate(Screen.AddEditCar.routeWithId(carId))
                                },
                                onNavigateToHistory = {
                                    navController.navigate(Screen.History.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        composable(
                            route = Screen.AddEditCar.route,
                            arguments = listOf(navArgument("carId") {
                                type = NavType.LongType; defaultValue = -1L
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
                            route = Screen.AddEditMaintenance.route,
                            arguments = listOf(
                                navArgument("carId") { type = NavType.LongType },
                                navArgument("recordId") { type = NavType.LongType; defaultValue = -1L }
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
