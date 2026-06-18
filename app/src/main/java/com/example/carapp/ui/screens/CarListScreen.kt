package com.example.carapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carapp.data.model.Car
import com.example.carapp.viewmodel.CarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CarListScreen(
    carViewModel: CarViewModel,
    onNavigateToAddCar: () -> Unit,
    onNavigateToEditCar: (Long) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val cars by carViewModel.cars.collectAsState()
    val selectedCarId by carViewModel.selectedCarId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cars") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCar) {
                Icon(Icons.Default.Add, "Add car")
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
                        Icons.Default.DirectionsCar, null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No cars added yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tap + to add your first car",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cars, key = { it.id }) { car ->
                    SwipeableCarCard(
                        car = car,
                        isSelected = car.id == (selectedCarId ?: cars.firstOrNull()?.id),
                        modifier = Modifier.animateItemPlacement(tween(300)),
                        onEdit = { onNavigateToEditCar(car.id) },
                        onSelect = {
                            carViewModel.selectCar(car.id)
                            onNavigateToHistory()
                        },
                        onDelete = {
                            carViewModel.deleteCar(car)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "${car.year} ${car.make} ${car.model} removed",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableCarCard(
    car: Car,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
                false // Don't dismiss yet — wait for confirmation
            } else false
        },
        positionalThreshold = { it * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        },
        content = {
            CarCard(
                car = car,
                isSelected = isSelected,
                onEdit = onEdit,
                onSelect = onSelect
            )
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Car?") },
            text = { Text("This will permanently delete ${car.year} ${car.make} ${car.model} and all its maintenance records.") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CarCard(
    car: Car,
    isSelected: Boolean,
    onEdit: () -> Unit,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${car.year} ${car.make} ${car.model}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text("Active", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if (car.color.isNotBlank()) {
                        Text(car.color, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit car")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                InfoPair("Mileage", "%,d mi".format(car.currentMileage))
                if (car.licensePlate.isNotBlank()) InfoPair("Plate", car.licensePlate)
                if (car.vin.isNotBlank()) InfoPair("VIN", car.vin.takeLast(6))
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("View History")
            }
        }
    }
}

@Composable
private fun InfoPair(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
