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
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
import com.example.carapp.viewmodel.CarViewModel
import com.example.carapp.viewmodel.MaintenanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceHistoryScreen(
    carId: Long,
    carViewModel: CarViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddMaintenance: (Long) -> Unit,
    onNavigateToEditMaintenance: (Long, Long) -> Unit
) {
    val cars by carViewModel.cars.collectAsState()
    val car = cars.find { it.id == carId }

    LaunchedEffect(carId) {
        maintenanceViewModel.setCarId(carId)
    }

    val records by maintenanceViewModel.records.collectAsState()

    var selectedFilter by remember { mutableStateOf<ServiceType?>(null) }
    val filtered = if (selectedFilter != null) records.filter { it.serviceType == selectedFilter } else records

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Maintenance History")
                        car?.let {
                            Text(
                                "${it.year} ${it.make} ${it.model}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
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
            FloatingActionButton(onClick = { onNavigateToAddMaintenance(carId) }) {
                Icon(Icons.Default.Add, contentDescription = "Add record")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (records.isNotEmpty()) {
                StatsSummaryRow(records = records, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                FilterChipRow(
                    records = records,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = if (it == selectedFilter) null else it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BuildCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (records.isEmpty()) "No maintenance records yet"
                            else "No records for selected filter"
                        )
                        if (records.isEmpty()) {
                            Button(
                                onClick = { onNavigateToAddMaintenance(carId) },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Add First Record")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { record ->
                        MaintenanceRecordCard(
                            record = record,
                            onEdit = { onNavigateToEditMaintenance(carId, record.id) },
                            onDelete = { maintenanceViewModel.deleteRecord(record) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSummaryRow(records: List<MaintenanceRecord>, modifier: Modifier = Modifier) {
    val totalCost = records.sumOf { it.cost }
    val currencyFormat = NumberFormat.getCurrencyInstance()

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Records", value = records.size.toString())
            StatItem(label = "Total Spent", value = currencyFormat.format(totalCost))
            StatItem(label = "Services", value = records.map { it.serviceType }.distinct().size.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipRow(
    records: List<MaintenanceRecord>,
    selectedFilter: ServiceType?,
    onFilterSelected: (ServiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    val serviceTypes = records.map { it.serviceType }.distinct()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        serviceTypes.forEach { type ->
            FilterChip(
                selected = selectedFilter == type,
                onClick = { onFilterSelected(type) },
                label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
private fun MaintenanceRecordCard(
    record: MaintenanceRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance()

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ServiceTypeIcon(record.serviceType, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.displayServiceName, fontWeight = FontWeight.SemiBold)
                    Text(
                        dateFormat.format(Date(record.serviceDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit",
                        modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (record.mileageAtService > 0) {
                    RecordDetail(label = "Mileage", value = "%,d mi".format(record.mileageAtService))
                }
                if (record.cost > 0) {
                    RecordDetail(label = "Cost", value = currencyFormat.format(record.cost))
                }
                if (record.shop.isNotBlank()) {
                    RecordDetail(label = "Shop", value = record.shop)
                }
            }

            if (record.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    record.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val nextDate = record.nextServiceDate
            val nextMileage = record.nextServiceMileage
            if (nextDate != null || nextMileage != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    nextDate?.let {
                        RecordDetail(label = "Next Date", value = dateFormat.format(Date(it)))
                    }
                    nextMileage?.let {
                        RecordDetail(label = "Next Mileage", value = "%,d mi".format(it))
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Record?") },
            text = { Text("Remove this ${record.displayServiceName} record?") },
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
private fun RecordDetail(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ServiceTypeIcon(serviceType: ServiceType, modifier: Modifier = Modifier) {
    val icon = when (serviceType) {
        ServiceType.OIL_CHANGE -> Icons.Default.WaterDrop
        ServiceType.TIRE_ROTATION, ServiceType.TIRE_REPLACEMENT, ServiceType.ALIGNMENT -> Icons.Default.TireRepair
        ServiceType.BRAKE_INSPECTION, ServiceType.BRAKE_REPLACEMENT -> Icons.Default.PanTool
        ServiceType.BATTERY -> Icons.Default.BatteryChargingFull
        ServiceType.AIR_FILTER, ServiceType.CABIN_FILTER -> Icons.Default.Air
        ServiceType.INSPECTION -> Icons.Default.FactCheck
        ServiceType.WIPER_BLADES -> Icons.Default.Visibility
        else -> Icons.Default.Build
    }
    Icon(icon, contentDescription = serviceType.displayName, modifier = modifier,
        tint = MaterialTheme.colorScheme.primary)
}
