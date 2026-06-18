package com.example.carapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carapp.data.model.Car
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
import com.example.carapp.viewmodel.CarViewModel
import com.example.carapp.viewmodel.MaintenanceViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryTabScreen(
    carViewModel: CarViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    onNavigateToAddMaintenance: (Long) -> Unit,
    onNavigateToEditMaintenance: (Long, Long) -> Unit,
    onNavigateToAddCar: () -> Unit
) {
    val cars by carViewModel.cars.collectAsState()
    val selectedCarId by carViewModel.selectedCarId.collectAsState()
    val selectedCar = cars.find { it.id == selectedCarId } ?: cars.firstOrNull()

    LaunchedEffect(selectedCar) {
        maintenanceViewModel.setCarId(selectedCar?.id)
    }

    val records by maintenanceViewModel.records.collectAsState()
    var selectedFilter by remember { mutableStateOf<ServiceType?>(null) }
    val filtered = if (selectedFilter != null) records.filter { it.serviceType == selectedFilter } else records

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("History")
                        selectedCar?.let {
                            Text(
                                "${it.year} ${it.make} ${it.model}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    if (cars.size > 1) {
                        CarPickerMenu(
                            cars = cars,
                            selectedCar = selectedCar,
                            onCarSelected = { carViewModel.selectCar(it.id) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedCar != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(onClick = { selectedCar?.let { onNavigateToAddMaintenance(it.id) } }) {
                    Icon(Icons.Default.Add, "Add record")
                }
            }
        }
    ) { paddingValues ->
        when {
            cars.isEmpty() -> {
                EmptyState(
                    icon = { Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                    message = "No cars yet",
                    actionLabel = "Add a car",
                    onAction = onNavigateToAddCar,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            records.isEmpty() -> {
                EmptyState(
                    icon = { Icon(Icons.Default.BuildCircle, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                    message = "No maintenance records yet",
                    actionLabel = "Log first service",
                    onAction = { selectedCar?.let { onNavigateToAddMaintenance(it.id) } },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    // Stats strip
                    HistoryStatsStrip(
                        records = records,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    // Filter chips
                    FilterRow(
                        serviceTypes = records.map { it.serviceType }.distinct(),
                        selectedFilter = selectedFilter,
                        onFilterChanged = { selectedFilter = if (it == selectedFilter) null else it },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                    // Records list
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { record ->
                            SwipeableRecordCard(
                                record = record,
                                modifier = Modifier.animateItemPlacement(tween(300)),
                                onEdit = {
                                    selectedCar?.let { car ->
                                        onNavigateToEditMaintenance(car.id, record.id)
                                    }
                                },
                                onDelete = {
                                    maintenanceViewModel.deleteRecord(record)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "${record.displayServiceName} deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            maintenanceViewModel.addRecord(record.copy(id = 0))
                                        }
                                    }
                                }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableRecordCard(
    record: MaintenanceRecord,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
        positionalThreshold = { it * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { DeleteBackground() },
        content = { ExpandableRecordCard(record = record, onEdit = onEdit) }
    )
}

@Composable
private fun DeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 20.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Delete", color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ExpandableRecordCard(record: MaintenanceRecord, onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val currency = NumberFormat.getCurrencyInstance()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Always-visible header
            Row(verticalAlignment = Alignment.CenterVertically) {
                ServiceTypeIcon(record.serviceType, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.displayServiceName, fontWeight = FontWeight.SemiBold)
                    Text(
                        dateFormat.format(Date(record.serviceDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (record.cost > 0) {
                    Text(
                        currency.format(record.cost),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Collapsed summary chips
            AnimatedVisibility(!expanded) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (record.mileageAtService > 0) {
                        SummaryChip(Icons.Default.Speed, "%,d mi".format(record.mileageAtService))
                    }
                    if (record.shop.isNotBlank()) {
                        SummaryChip(Icons.Default.Store, record.shop)
                    }
                }
            }

            // Expanded details
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (record.mileageAtService > 0)
                            DetailItem("Mileage", "%,d mi".format(record.mileageAtService))
                        if (record.cost > 0)
                            DetailItem("Cost", currency.format(record.cost))
                        if (record.shop.isNotBlank())
                            DetailItem("Shop", record.shop)
                    }

                    if (record.notes.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            record.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val nextDate = record.nextServiceDate
                    val nextMileage = record.nextServiceMileage
                    if (nextDate != null || nextMileage != null) {
                        Spacer(Modifier.height(8.dp))
                        SectionLabel("Next Service")
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            nextDate?.let { DetailItem("Date", dateFormat.format(Date(it))) }
                            nextMileage?.let { DetailItem("Mileage", "%,d mi".format(it)) }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Edit Record")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(0.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(3.dp))
        Text(text, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    serviceTypes: List<ServiceType>,
    selectedFilter: ServiceType?,
    onFilterChanged: (ServiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    if (serviceTypes.size <= 1) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        serviceTypes.forEach { type ->
            FilterChip(
                selected = selectedFilter == type,
                onClick = { onFilterChanged(type) },
                label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
private fun HistoryStatsStrip(records: List<MaintenanceRecord>, modifier: Modifier = Modifier) {
    val currency = NumberFormat.getCurrencyInstance()
    val total = records.sumOf { it.cost }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearCost = records
        .filter { Calendar.getInstance().apply { timeInMillis = it.serviceDate }.get(Calendar.YEAR) == currentYear }
        .sumOf { it.cost }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("Records", records.size.toString())
            StatItem("This Year", currency.format(yearCost))
            StatItem("All Time", currency.format(total))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CarPickerMenu(
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
                            } else {
                                Spacer(Modifier.width(22.dp))
                            }
                            Text("${car.year} ${car.make} ${car.model}")
                        }
                    },
                    onClick = { onCarSelected(car); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: @Composable () -> Unit,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Spacer(Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
