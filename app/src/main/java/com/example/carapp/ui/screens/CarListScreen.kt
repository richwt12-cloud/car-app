package com.example.carapp.ui.screens

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
import com.example.carapp.viewmodel.CarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    carViewModel: CarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddCar: () -> Unit,
    onNavigateToEditCar: (Long) -> Unit,
    onNavigateToHistory: (Long) -> Unit
) {
    val cars by carViewModel.cars.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cars") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCar) {
                Icon(Icons.Default.Add, contentDescription = "Add car")
            }
        }
    ) { paddingValues ->
        if (cars.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No cars added yet")
                    Button(onClick = onNavigateToAddCar, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Add Your First Car")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cars, key = { it.id }) { car ->
                    CarCard(
                        car = car,
                        onEdit = { onNavigateToEditCar(car.id) },
                        onDelete = { carViewModel.deleteCar(car) },
                        onViewHistory = { onNavigateToHistory(car.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CarCard(
    car: Car,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${car.year} ${car.make} ${car.model}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (car.color.isNotBlank()) {
                        Text(car.color, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(label = "Mileage", value = "%,d mi".format(car.currentMileage))
                if (car.licensePlate.isNotBlank()) {
                    InfoChip(label = "Plate", value = car.licensePlate)
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onViewHistory, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("View History")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Car?") },
            text = { Text("This will permanently delete ${car.year} ${car.make} ${car.model} and all its maintenance records.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
