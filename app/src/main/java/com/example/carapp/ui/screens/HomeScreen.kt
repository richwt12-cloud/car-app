package com.example.carapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
import com.example.carapp.ui.theme.*
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
                title = {
                    Column {
                        Text("Good day!", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        Text("Car Maintenance", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (cars.size > 1) {
                        CarSwitcherButton(cars = cars, selectedCar = selectedCar,
                            onCarSelected = { carViewModel.selectCar(it.id) })
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
            AnimatedVisibility(visible = selectedCar != null, enter = scaleIn(), exit = scaleOut()) {
                ExtendedFloatingActionButton(
                    onClick = { selectedCar?.let { onNavigateToAddMaintenance(it.id) } },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Log Service") }
                )
            }
        }
    ) { padding ->
        if (cars.isEmpty()) {
            EmptyGarageScreen(onNavigateToAddCar = onNavigateToAddCar,
                modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    selectedCar?.let { car ->
                        CarHeroCard(
                            car = car,
                            recordCount = records.size,
                            onEditCar = onNavigateToAddCar,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                item {
                    selectedCar?.let { car ->
                        QuickLogSection(
                            car = car,
                            onNavigateToAddMaintenance = onNavigateToAddMaintenance,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                if (records.isNotEmpty()) {
                    item {
                        YearSpendBanner(
                            records = records,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                if (upcomingRecords.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("What's Due", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            TextButton(onClick = onNavigateToHistory) { Text("View all") }
                        }
                    }
                    items(upcomingRecords) { record ->
                        UpcomingServiceCard(
                            record = record,
                            currentMileage = selectedCar?.currentMileage ?: 0,
                            onClick = onNavigateToHistory,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                } else if (selectedCar != null) {
                    item {
                        AllClearCard(
                            onViewHistory = onNavigateToHistory,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Hero Card ──────────────────────────────────────────────────────────────

@Composable
private fun CarHeroCard(
    car: Car,
    recordCount: Int,
    onEditCar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gradient = Brush.linearGradient(
        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.55f))
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(130.dp)
                    .padding(bottom = 8.dp, end = 12.dp),
                tint = Color.White.copy(alpha = 0.1f)
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${car.year}  •  ${if (car.color.isNotBlank()) car.color else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f))
                        Text("${car.make} ${car.model}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White)
                    }
                    FilledIconButton(
                        onClick = onEditCar,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Edit, "Edit car", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroPill(label = "Mileage", value = "%,d mi".format(car.currentMileage))
                    if (car.licensePlate.isNotBlank()) {
                        HeroPill(label = "Plate", value = car.licensePlate)
                    }
                    HeroPill(label = "Services", value = recordCount.toString())
                }
            }
        }
    }
}

@Composable
private fun HeroPill(label: String, value: String) {
    Surface(shape = RoundedCornerShape(50.dp), color = Color.White.copy(alpha = 0.18f)) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f))
        }
    }
}

// ── Quick Log ──────────────────────────────────────────────────────────────

private val quickServices = listOf(
    ServiceType.OIL_CHANGE,
    ServiceType.TIRE_ROTATION,
    ServiceType.BRAKE_INSPECTION,
    ServiceType.BATTERY,
    ServiceType.INSPECTION,
    ServiceType.OTHER
)

@Composable
private fun QuickLogSection(
    car: Car,
    onNavigateToAddMaintenance: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Quick Log", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp, top = 4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickServices) { type ->
                QuickServiceChip(serviceType = type, onClick = { onNavigateToAddMaintenance(car.id) })
            }
        }
    }
}

@Composable
private fun QuickServiceChip(serviceType: ServiceType, onClick: () -> Unit) {
    val color = serviceTypeColor(serviceType)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ServiceTypeIcon(serviceType, modifier = Modifier.size(16.dp), tint = color)
            Text(serviceType.displayName, style = MaterialTheme.typography.labelMedium,
                color = color, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Year Spend Banner ──────────────────────────────────────────────────────

@Composable
private fun YearSpendBanner(records: List<MaintenanceRecord>, modifier: Modifier = Modifier) {
    val currency = NumberFormat.getCurrencyInstance()
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val yearSpend = records
        .filter { Calendar.getInstance().apply { timeInMillis = it.serviceDate }.get(Calendar.YEAR) == year }
        .sumOf { it.cost }
    val allTimeSpend = records.sumOf { it.cost }

    if (allTimeSpend <= 0.0) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SpendCard("$year Spend", currency.format(yearSpend),
            Icons.Default.CalendarMonth, Modifier.weight(1f))
        SpendCard("All Time", currency.format(allTimeSpend),
            Icons.Default.Receipt, Modifier.weight(1f))
    }
}

@Composable
private fun SpendCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column {
                Text(value, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

// ── Upcoming Service Card ──────────────────────────────────────────────────

@Composable
private fun UpcomingServiceCard(
    record: MaintenanceRecord,
    currentMileage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val isDueByDate = record.nextServiceDate != null && record.nextServiceDate <= today
    val isDueByMileage = record.nextServiceMileage != null && currentMileage >= record.nextServiceMileage
    val isOverdue = isDueByDate || isDueByMileage
    val statusColor = if (isOverdue) OverduRed else UpcomingOrange
    val serviceColor = serviceTypeColor(record.serviceType)

    val mileageProgress = record.nextServiceMileage?.let { nextMi ->
        val range = (nextMi - record.mileageAtService).toFloat()
        if (range > 0) ((currentMileage - record.mileageAtService).toFloat() / range).coerceIn(0f, 1f) else 1f
    }
    val dateProgress = record.nextServiceDate?.let { nextDate ->
        val range = (nextDate - record.serviceDate).toFloat()
        if (range > 0) ((today - record.serviceDate).toFloat() / range).coerceIn(0f, 1f) else 1f
    }
    val progress = max(mileageProgress ?: 0f, dateProgress ?: 0f)
    val animatedProgress by animateFloatAsState(progress, tween(900, easing = FastOutSlowInEasing), label = "p")

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp,
            statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(serviceColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    ServiceTypeIcon(record.serviceType,
                        modifier = Modifier.size(22.dp), tint = serviceColor)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.displayServiceName, fontWeight = FontWeight.SemiBold)
                    record.nextServiceDate?.let {
                        Text(
                            if (isDueByDate) "Was due ${dateFormat.format(Date(it))}"
                            else "Due ${dateFormat.format(Date(it))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    record.nextServiceMileage?.let {
                        val delta = it - currentMileage
                        Text(
                            if (delta <= 0) "${"%,d".format(-delta)} mi overdue"
                            else "${"%,d".format(delta)} mi remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        if (isOverdue) "OVERDUE" else "UPCOMING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.12f)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Last service", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${(animatedProgress * 100).toInt()}% of interval",
                    style = MaterialTheme.typography.labelSmall, color = statusColor)
            }
        }
    }
}

// ── All Clear ─────────────────────────────────────────────────────────────

@Composable
private fun AllClearCard(onViewHistory: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onViewHistory),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoodGreen.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoodGreen.copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoodGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = GoodGreen, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("All caught up!", fontWeight = FontWeight.Bold,
                    color = GoodGreen)
                Text("No upcoming maintenance due",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Empty State ────────────────────────────────────────────────────────────

@Composable
private fun EmptyGarageScreen(onNavigateToAddCar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.DirectionsCar, null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.height(24.dp))
        Text("Your garage is empty", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Add a car to start tracking maintenance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onNavigateToAddCar, contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add Your Car", style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ── Car Switcher ──────────────────────────────────────────────────────────

@Composable
private fun CarSwitcherButton(
    cars: List<Car>,
    selectedCar: Car?,
    onCarSelected: (Car) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.SwapHoriz, "Switch car")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cars.forEach { car ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (car.id == selectedCar?.id) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                            } else Spacer(Modifier.width(22.dp))
                            Text("${car.year} ${car.make} ${car.model}")
                        }
                    },
                    onClick = { onCarSelected(car); expanded = false }
                )
            }
        }
    }
}
