package com.example.carapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
import com.example.carapp.ui.theme.serviceTypeColor
import com.example.carapp.viewmodel.CarViewModel
import com.example.carapp.viewmodel.MaintenanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMaintenanceScreen(
    carId: Long,
    recordId: Long?,
    carViewModel: CarViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    onNavigateBack: () -> Unit
) {
    val isEdit = recordId != null && recordId > 0
    val cars by carViewModel.cars.collectAsState()
    val car = cars.find { it.id == carId }

    var selectedServiceType by remember { mutableStateOf(ServiceType.OIL_CHANGE) }
    var customServiceName by remember { mutableStateOf("") }
    var serviceDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var mileage by remember { mutableStateOf(car?.currentMileage?.toString() ?: "") }
    var cost by remember { mutableStateOf("") }
    var shop by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nextServiceDate by remember { mutableStateOf<Long?>(null) }
    var nextServiceMileage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNextDatePicker by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(recordId) {
        if (isEdit && !initialized) {
            maintenanceViewModel.getRecordById(recordId!!)?.let { record ->
                selectedServiceType = record.serviceType
                customServiceName = record.customServiceName
                serviceDate = record.serviceDate
                mileage = record.mileageAtService.toString()
                cost = if (record.cost > 0) record.cost.toString() else ""
                shop = record.shop
                notes = record.notes
                nextServiceDate = record.nextServiceDate
                nextServiceMileage = record.nextServiceMileage?.toString() ?: ""
            }
            initialized = true
        }
    }

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun save() {
        val record = MaintenanceRecord(
            id = if (isEdit) recordId!! else 0,
            carId = carId,
            serviceType = selectedServiceType,
            customServiceName = customServiceName.trim(),
            serviceDate = serviceDate,
            mileageAtService = mileage.toIntOrNull() ?: 0,
            cost = cost.toDoubleOrNull() ?: 0.0,
            shop = shop.trim(),
            notes = notes.trim(),
            nextServiceDate = nextServiceDate,
            nextServiceMileage = nextServiceMileage.toIntOrNull()
        )
        if (isEdit) maintenanceViewModel.updateRecord(record) else maintenanceViewModel.addRecord(record)
        val newMileage = mileage.toIntOrNull() ?: 0
        if (car != null && newMileage > car.currentMileage) {
            carViewModel.updateCar(car.copy(currentMileage = newMileage))
        }
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Record" else "Log Service") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = ::save,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Service Type Grid
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                SectionHeader("Service Type")
                Spacer(Modifier.height(10.dp))
                ServiceTypeGrid(
                    selected = selectedServiceType,
                    onSelected = { selectedServiceType = it }
                )
            }

            if (selectedServiceType == ServiceType.OTHER) {
                OutlinedTextField(
                    value = customServiceName,
                    onValueChange = { customServiceName = it },
                    label = { Text("Service Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Date
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Date & Mileage")
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = dateFormat.format(Date(serviceDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Service Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null,
                        tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Edit, "Pick date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = mileage,
                        onValueChange = { mileage = it.filter { c -> c.isDigit() } },
                        label = { Text("Mileage") },
                        leadingIcon = { Icon(Icons.Default.Speed, null,
                            tint = MaterialTheme.colorScheme.primary) },
                        suffix = { Text("mi") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cost") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null,
                            tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next),
                        singleLine = true
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Details
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Details")
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = shop,
                    onValueChange = { shop = it },
                    label = { Text("Shop / Mechanic") },
                    leadingIcon = { Icon(Icons.Default.Store, null,
                        tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    leadingIcon = { Icon(Icons.Default.Notes, null,
                        tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Next Service Reminder
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NotificationsActive, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    SectionHeader("Next Service Reminder")
                }
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = if (nextServiceDate != null) dateFormat.format(Date(nextServiceDate!!)) else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Next Service Date") },
                    placeholder = { Text("Optional") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null,
                        tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        Row {
                            if (nextServiceDate != null) {
                                IconButton(onClick = { nextServiceDate = null }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                            IconButton(onClick = { showNextDatePicker = true }) {
                                Icon(Icons.Default.Edit, "Pick date")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = nextServiceMileage,
                    onValueChange = { nextServiceMileage = it.filter { c -> c.isDigit() } },
                    label = { Text("Next Service Mileage") },
                    placeholder = { Text("Optional") },
                    leadingIcon = { Icon(Icons.Default.Speed, null,
                        tint = MaterialTheme.colorScheme.primary) },
                    suffix = { Text("mi") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = ::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) "Save Changes" else "Log Service",
                    style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        ServiceDatePickerDialog(
            initialDate = serviceDate,
            onDateSelected = { serviceDate = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showNextDatePicker) {
        ServiceDatePickerDialog(
            initialDate = nextServiceDate ?: System.currentTimeMillis(),
            onDateSelected = { nextServiceDate = it; showNextDatePicker = false },
            onDismiss = { showNextDatePicker = false }
        )
    }
}

// ── Service Type Grid ──────────────────────────────────────────────────────

@Composable
private fun ServiceTypeGrid(selected: ServiceType, onSelected: (ServiceType) -> Unit) {
    val services = ServiceType.values().toList()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        services.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { type ->
                    ServiceTypeCell(
                        serviceType = type,
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ServiceTypeCell(
    serviceType: ServiceType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = serviceTypeColor(serviceType)
    val bgColor = if (selected) color.copy(alpha = 0.14f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (selected) BorderStroke(2.dp, color) else BorderStroke(1.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ServiceTypeIcon(
                serviceType = serviceType,
                modifier = Modifier.size(24.dp),
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                serviceType.displayName,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceDatePickerDialog(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onDateSelected(it) } }) {
                Text("OK")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = state)
    }
}
