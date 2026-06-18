package com.example.carapp.ui.screens

import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
                title = {
                    Column {
                        Text("My Garage", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text("${cars.size} car${if (cars.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddCar,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Car") }
            )
        }
    ) { paddingValues ->
        if (cars.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(100.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Your garage is empty", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap + to add your first car",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(cars, key = { it.id }) { car ->
                    val isSelected = car.id == (selectedCarId ?: cars.firstOrNull()?.id)
                    SwipeableCarCard(
                        car = car,
                        isSelected = isSelected,
                        modifier = Modifier.animateItemPlacement(tween(300)),
                        onEdit = { onNavigateToEditCar(car.id) },
                        onSelect = { carViewModel.selectCar(car.id); onNavigateToHistory() },
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
            if (value == SwipeToDismissBoxValue.EndToStart) { showDeleteDialog = true; false }
            else false
        },
        positionalThreshold = { it * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(modifier = Modifier.padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, null,
                        tint = MaterialTheme.colorScheme.onErrorContainer)
                    Text("Remove", color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    ) {
        CarCard(car = car, isSelected = isSelected, onEdit = onEdit, onSelect = onSelect)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Car?") },
            text = { Text("This will permanently delete ${car.year} ${car.make} ${car.model} and all its records.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CarCard(car: Car, isSelected: Boolean, onEdit: () -> Unit, onSelect: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gradient = Brush.linearGradient(
        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier.border(2.dp, primaryColor, RoundedCornerShape(20.dp))
                  else Modifier),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, null,
                    modifier = Modifier.align(Alignment.CenterEnd).size(60.dp),
                    tint = Color.White.copy(alpha = 0.15f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${car.year % 100}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${car.make} ${car.model}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Surface(shape = RoundedCornerShape(50.dp),
                                    color = Color.White.copy(alpha = 0.25f)) {
                                    Text("Active", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (car.color.isNotBlank()) {
                                Text(car.color, style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                    IconButton(onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn("Mileage", "%,d mi".format(car.currentMileage),
                    Modifier.weight(1f))
                if (car.licensePlate.isNotBlank()) {
                    VerticalDivider(modifier = Modifier.height(32.dp))
                    StatColumn("Plate", car.licensePlate, Modifier.weight(1f))
                }
                if (car.vin.isNotBlank()) {
                    VerticalDivider(modifier = Modifier.height(32.dp))
                    StatColumn("VIN", "···${car.vin.takeLast(4)}", Modifier.weight(1f))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("View Maintenance History")
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
