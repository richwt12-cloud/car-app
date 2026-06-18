package com.example.carapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carapp.data.model.MaintenanceRecord
import com.example.carapp.data.model.ServiceType
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

    var serviceTypeMenuExpanded by remember { mutableStateOf(false) }
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
        if (isEdit) maintenanceViewModel.updateRecord(record)
        else maintenanceViewModel.addRecord(record)

        val newMileage = mileage.toIntOrNull() ?: 0
        if (car != null && newMileage > car.currentMileage) {
            carViewModel.updateCar(car.copy(currentMileage = newMileage))
        }
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Record" else "Add Maintenance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = ::save) {
                        Icon(Icons.Default.Save, "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Service Details", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            ExposedDropdownMenuBox(
                expanded = serviceTypeMenuExpanded,
                onExpandedChange = { serviceTypeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedServiceType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Service Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceTypeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = {
                        ServiceTypeIcon(selectedServiceType, modifier = Modifier.size(20.dp))
                    }
                )
                ExposedDropdownMenu(
                    expanded = serviceTypeMenuExpanded,
                    onDismissRequest = { serviceTypeMenuExpanded = false }
                ) {
                    ServiceType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            leadingIcon = { ServiceTypeIcon(type, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                selectedServiceType = type
                                serviceTypeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedServiceType == ServiceType.OTHER) {
                OutlinedTextField(
                    value = customServiceName,
                    onValueChange = { customServiceName = it },
                    label = { Text("Service Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = dateFormat.format(Date(serviceDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Service Date *") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Text("Details", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = mileage,
                    onValueChange = { mileage = it.filter { c -> c.isDigit() } },
                    label = { Text("Mileage") },
                    suffix = { Text("mi") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost") },
                    prefix = { Text("$") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = shop,
                onValueChange = { shop = it },
                label = { Text("Shop / Mechanic") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            HorizontalDivider()
            Text("Next Service Reminder", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = if (nextServiceDate != null) dateFormat.format(Date(nextServiceDate!!)) else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Next Service Date") },
                placeholder = { Text("Optional") },
                trailingIcon = {
                    Row {
                        if (nextServiceDate != null) {
                            IconButton(onClick = { nextServiceDate = null }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                        IconButton(onClick = { showNextDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, "Pick date")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nextServiceMileage,
                onValueChange = { nextServiceMileage = it.filter { c -> c.isDigit() } },
                label = { Text("Next Service Mileage") },
                placeholder = { Text("Optional") },
                suffix = { Text("mi") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            Button(onClick = ::save, modifier = Modifier.fillMaxWidth()) {
                Text(if (isEdit) "Save Changes" else "Add Record")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = serviceDate,
            onDateSelected = { serviceDate = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showNextDatePicker) {
        DatePickerDialog(
            initialDate = nextServiceDate ?: System.currentTimeMillis(),
            onDateSelected = { nextServiceDate = it; showNextDatePicker = false },
            onDismiss = { showNextDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it) }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
