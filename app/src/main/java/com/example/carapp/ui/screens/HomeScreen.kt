package com.example.carapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carapp.data.model.Car
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
import com.example.carapp.ui.theme.GoodGreen
import com.example.carapp.ui.theme.OverduRed
import com.example.carapp.ui.theme.UpcomingOrange
import com.example.carapp.viewmodel.CarViewModel
import com.example.carapp.viewmodel.MaintenanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    carViewModel: CarViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    onNavigateToCarList: () -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    onNavigateToAddMaintenance: (Long) -> Unit,
    onNavigateToAddCar: () -> Unit
) {
    val cars by carViewModel.cars.collectAsState()
    val selectedCarId by carViewModel.selectedCarId.collectAsState()
    val selectedCar = cars.find { it.id == selectedCarId } ?: cars.firstOrNull()

    LaunchedEffect(selectedCar) {
        maintenanceViewModel.setCarId(selectedCar?.id)
    }

    val upcomingRecords by maintenanceViewModel.upcomingRecords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Maintenance") },
                actions = {
                    IconButton(onClick = onNavigateToCarList) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = "My Cars")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (selectedCar != null) {
                FloatingActionButton(
                    onClick = { onNavigateToAddMaintenance(selectedCar.id) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add maintenance record")
                }
            }
        }
    ) { paddingValues ->
        if (cars.isEmpty()) {
            EmptyGarage(onNavigateToAddCar = onNavigateToAddCar, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CarSelectorCard(
                        cars = cars,
                        selectedCar = selectedCar,
                        onCarSelected = { carViewModel.selectCar(it.id) },
                        onViewHistory = { selectedCar?.let { onNavigateToHistory(it.id) } },
                        onAddCar = onNavigateToAddCar
                    )
                }

                if (upcomingRecords.isNotEmpty()) {
                    item {
                        Text(
                            text = "Upcoming / Due",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(upcomingRecords) { record ->
                        UpcomingMaintenanceCard(
                            record = record,
                            currentMileage = selectedCar?.currentMileage ?: 0,
                            onClick = {
                                selectedCar?.let {
                                    onNavigateToHistory(it.id)
                                }
                            }
                        )
                    }
                } else if (selectedCar != null) {
                    item {
                        AllClearCard()
                    }
                }

                item {
                    if (selectedCar != null) {
                        OutlinedButton(
                            onClick = { onNavigateToHistory(selectedCar.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View Full History")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGarage(onNavigateToAddCar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text("No cars yet", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Add your first car to start tracking",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNavigateToAddCar) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Car")
        }
    }
}

@Composable
private fun CarSelectorCard(
    cars: List<Car>,
    selectedCar: Car?,
    onCarSelected: (Car) -> Unit,
    onViewHistory: () -> Unit,
    onAddCar: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (selectedCar != null) {
                        Text(
                            "${selectedCar.year} ${selectedCar.make} ${selectedCar.model}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${"%,d".format(selectedCar.currentMileage)} mi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (cars.size > 1) {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Switch car")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            cars.forEach { car ->
                                DropdownMenuItem(
                                    text = { Text("${car.year} ${car.make} ${car.model}") },
                                    onClick = { onCarSelected(car); expanded = false }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Add another car") },
                                leadingIcon = { Icon(Icons.Default.Add, null) },
                                onClick = { onAddCar(); expanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingMaintenanceCard(
    record: MaintenanceRecord,
    currentMileage: Int,
    onClick: () -> Unit
) {
    val today = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    val isDueByDate = record.nextServiceDate != null && record.nextServiceDate <= today
    val isDueByMileage = record.nextServiceMileage != null && currentMileage >= record.nextServiceMileage
    val isOverdue = isDueByDate || isDueByMileage

    val statusColor = if (isOverdue) OverduRed else UpcomingOrange

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Schedule,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(record.displayServiceName, fontWeight = FontWeight.SemiBold)
                record.nextServiceDate?.let {
                    Text(
                        "Due: ${dateFormat.format(Date(it))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                record.nextServiceMileage?.let {
                    Text(
                        "Due at: ${"%,d".format(it)} mi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                if (isOverdue) "OVERDUE" else "UPCOMING",
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AllClearCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoodGreen.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GoodGreen,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("All caught up!", fontWeight = FontWeight.SemiBold)
                Text(
                    "No upcoming maintenance due",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
