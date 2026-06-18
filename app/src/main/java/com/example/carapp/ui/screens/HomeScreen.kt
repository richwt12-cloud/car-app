package com.example.carapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.carapp.ui.theme.GoodGreen
import com.example.carapp.ui.theme.OverduRed
import com.example.carapp.ui.theme.UpcomingOrange
import com.example.carapp.viewmodel.CarViewModel
import com.example.carapp.viewmodel.MaintenanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    carViewModel: CarViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    onNavigateToAddMaintenance: (Long) -> Unit,
    onNavigateToAddCar: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val cars by carViewModel.cars.collectAsState()
    val selectedCarId by carViewModel.selectedCarId.collectAsState()
    val selectedCar = cars.find { it.id == selectedCarId } ?: cars.firstOrNull()

    LaunchedEffect(selectedCar) {
        maintenanceViewModel.setCarId(selectedCar?.id)
    }

    val records by maintenanceViewModel.records.collectAsState()
    val upcomingRecords by maintenanceViewModel.upcomingRecords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Maintenance") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedCar != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(onClick = { selectedCar?.let { onNavigateToAddMaintenance(it.id) } }) {
                    Icon(Icons.Default.Add, contentDescription = "Add maintenance record")
                }
            }
        }
    ) { paddingValues ->
        if (cars.isEmpty()) {
            EmptyGarageScreen(onNavigateToAddCar = onNavigateToAddCar, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CarSelectorCard(
                        cars = cars,
                        selectedCar = selectedCar,
                        onCarSelected = { carViewModel.selectCar(it.id) },
                        onAddCar = onNavigateToAddCar
                    )
                }

                if (selectedCar != null && records.isNotEmpty()) {
                    item {
                        QuickStatsRow(
                            records = records,
                            currentMileage = selectedCar.currentMileage
                        )
                    }
                }

                if (upcomingRecords.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Upcoming / Due",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onNavigateToHistory) {
                                Text("See all")
                            }
                        }
                    }
                    items(upcomingRecords) { record ->
                        UpcomingServiceCard(
                            record = record,
                            currentMileage = selectedCar?.currentMileage ?: 0,
                            onClick = onNavigateToHistory
                        )
                    }
                } else if (selectedCar != null) {
                    item { AllClearCard(onViewHistory = onNavigateToHistory) }
                }
            }
        }
    }
}

@Composable
private fun EmptyGarageScreen(onNavigateToAddCar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.DirectionsCar,
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
            Icon(Icons.Default.Add, null)
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
    onAddCar: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
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
                        Icon(Icons.Default.SwapHoriz, "Switch car")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        cars.forEach { car ->
                            DropdownMenuItem(
                                text = { Text("${car.year} ${car.make} ${car.model}") },
                                onClick = { onCarSelected(car); expanded = false }
                            )
                        }
                        HorizontalDivider()
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

@Composable
private fun QuickStatsRow(records: List<MaintenanceRecord>, currentMileage: Int) {
    val totalCost = records.sumOf { it.cost }
    val currency = NumberFormat.getCurrencyInstance()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearCost = records
        .filter { Calendar.getInstance().apply { timeInMillis = it.serviceDate }.get(Calendar.YEAR) == currentYear }
        .sumOf { it.cost }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            label = "This Year",
            value = currency.format(yearCost),
            icon = Icons.Default.AttachMoney,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "All Time",
            value = currency.format(totalCost),
            icon = Icons.Default.Receipt,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Records",
            value = records.size.toString(),
            icon = Icons.Default.List,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UpcomingServiceCard(
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

    // Calculate progress (how far through the current service interval we are)
    val mileageProgress = record.nextServiceMileage?.let { nextMi ->
        val lastMi = record.mileageAtService
        val range = (nextMi - lastMi).toFloat()
        if (range > 0) ((currentMileage - lastMi).toFloat() / range).coerceIn(0f, 1f) else 1f
    }
    val dateProgress = record.nextServiceDate?.let { nextDate ->
        val lastDate = record.serviceDate
        val range = (nextDate - lastDate).toFloat()
        if (range > 0) ((today - lastDate).toFloat() / range).coerceIn(0f, 1f) else 1f
    }
    val progress = max(mileageProgress ?: 0f, dateProgress ?: 0f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.07f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isOverdue) Icons.Default.Warning else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.displayServiceName, fontWeight = FontWeight.SemiBold)
                    record.nextServiceDate?.let {
                        Text(
                            if (isOverdue) "Was due ${dateFormat.format(Date(it))}"
                            else "Due ${dateFormat.format(Date(it))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    record.nextServiceMileage?.let {
                        val delta = it - currentMileage
                        Text(
                            if (delta <= 0) "Due at ${"%,d".format(it)} mi (${"%,d".format(-delta)} mi overdue)"
                            else "${"%,d".format(delta)} mi remaining (at ${"%,d".format(it)} mi)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Badge(containerColor = statusColor) {
                    Text(
                        if (isOverdue) "OVERDUE" else "UPCOMING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Last service",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${(animatedProgress * 100).toInt()}% of interval",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun AllClearCard(onViewHistory: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onViewHistory),
        colors = CardDefaults.cardColors(containerColor = GoodGreen.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = GoodGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("All caught up!", fontWeight = FontWeight.SemiBold)
                Text(
                    "No upcoming maintenance due",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
