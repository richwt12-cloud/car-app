package com.example.carapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carapp.data.model.Car
import com.example.carapp.ui.theme.GradientDeep
import com.example.carapp.ui.theme.GradientLight
import com.example.carapp.viewmodel.CarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarScreen(
    carId: Long?,
    carViewModel: CarViewModel,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var existingCar by remember { mutableStateOf<Car?>(null) }
    val isEdit = carId != null && carId > 0

    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var currentMileage by remember { mutableStateOf("") }

    var makeError by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf(false) }
    var yearError by remember { mutableStateOf(false) }

    LaunchedEffect(carId) {
        if (isEdit) {
            carViewModel.cars.value.find { it.id == carId }?.let { car ->
                existingCar = car
                make = car.make
                model = car.model
                year = car.year.toString()
                licensePlate = car.licensePlate
                vin = car.vin
                color = car.color
                currentMileage = car.currentMileage.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(GradientDeep, GradientLight),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
                title = {
                    Text(
                        if (isEdit) "Edit Car" else "Add Car",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        makeError = make.isBlank()
                        modelError = model.isBlank()
                        yearError = year.toIntOrNull() == null

                        if (!makeError && !modelError && !yearError) {
                            val car = Car(
                                id = existingCar?.id ?: 0,
                                make = make.trim(),
                                model = model.trim(),
                                year = year.toInt(),
                                licensePlate = licensePlate.trim(),
                                vin = vin.trim(),
                                color = color.trim(),
                                currentMileage = currentMileage.toIntOrNull() ?: 0
                            )
                            if (isEdit) carViewModel.updateCar(car) else carViewModel.addCar(car)
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
            Text("Vehicle Info", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = make,
                onValueChange = { make = it; makeError = false },
                label = { Text("Make *") },
                placeholder = { Text("e.g., Toyota") },
                isError = makeError,
                supportingText = if (makeError) {{ Text("Required") }} else null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it; modelError = false },
                label = { Text("Model *") },
                placeholder = { Text("e.g., Camry") },
                isError = modelError,
                supportingText = if (modelError) {{ Text("Required") }} else null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it; yearError = false },
                    label = { Text("Year *") },
                    placeholder = { Text("2020") },
                    isError = yearError,
                    supportingText = if (yearError) {{ Text("Valid year") }} else null,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    placeholder = { Text("Silver") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
            }

            HorizontalDivider()
            Text("Current Mileage & ID", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = currentMileage,
                onValueChange = { currentMileage = it.filter { c -> c.isDigit() } },
                label = { Text("Current Mileage") },
                placeholder = { Text("e.g., 45000") },
                suffix = { Text("mi") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it.uppercase() },
                label = { Text("License Plate") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = vin,
                onValueChange = { vin = it.uppercase() },
                label = { Text("VIN (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    makeError = make.isBlank()
                    modelError = model.isBlank()
                    yearError = year.toIntOrNull() == null

                    if (!makeError && !modelError && !yearError) {
                        val car = Car(
                            id = existingCar?.id ?: 0,
                            make = make.trim(),
                            model = model.trim(),
                            year = year.toInt(),
                            licensePlate = licensePlate.trim(),
                            vin = vin.trim(),
                            color = color.trim(),
                            currentMileage = currentMileage.toIntOrNull() ?: 0
                        )
                        if (isEdit) carViewModel.updateCar(car) else carViewModel.addCar(car)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Car")
            }
        }
    }
}
