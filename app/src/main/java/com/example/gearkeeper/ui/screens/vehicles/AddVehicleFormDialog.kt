package com.example.gearkeeper.ui.screens.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.BrandOption
import com.example.gearkeeper.domain.preferences.DistanceUnit
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import com.example.gearkeeper.data.local.ModelOption

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddVehicleFormDialog(
    dialogTitle: String,
    state: VehicleFormState,
    errors: VehicleFormErrors,
    brands: List<BrandOption>,
    models: List<ModelOption>,
    submitError: String?,
    onStateChange: (VehicleFormState) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
): Unit {
    val fuelOptions: List<Pair<String, String>> = listOf(
        "Gasoline" to stringResource(id = R.string.fuel_gasoline),
        "Diesel" to stringResource(id = R.string.fuel_diesel),
        "Hybrid" to stringResource(id = R.string.fuel_hybrid),
        "Electric" to stringResource(id = R.string.fuel_electric),
        "Flex" to stringResource(id = R.string.fuel_flex),
        "Other" to stringResource(id = R.string.fuel_other),
    )
    val transmissionOptions: List<Pair<String, String>> = listOf(
        "Automatic" to stringResource(id = R.string.transmission_automatic),
        "Manual" to stringResource(id = R.string.transmission_manual),
    )
    val brandOptions: List<Pair<String, String>> =
        brands.map { option: BrandOption -> option.name to option.name }
    val modelOptions: List<Pair<String, String>> =
        models.map { option: ModelOption -> option.name to option.name }
    val reminderIntervalUnitOptions: List<Pair<OdometerReminderIntervalUnitUi, String>> = listOf(
        OdometerReminderIntervalUnitUi.Days to stringResource(id = R.string.vehicle_odometer_reminder_unit_days),
        OdometerReminderIntervalUnitUi.Months to stringResource(id = R.string.vehicle_odometer_reminder_unit_months),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val distanceUnit: DistanceUnit = LocalDistanceUnit.current
                val currentOdometerLabel: String = when (distanceUnit) {
                    DistanceUnit.METRIC_KM -> stringResource(id = R.string.current_odometer_label_km)
                    DistanceUnit.IMPERIAL_MILES -> stringResource(id = R.string.current_odometer_label_mi)
                }
                val errorColor = MaterialTheme.colorScheme.error
                if (submitError != null) {
                    Text(
                        text = submitError,
                        color = errorColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                VehicleTypeSelector(
                    selectedType = state.vehicleType,
                    onSelected = { selectedType: VehicleTypeUi ->
                        onStateChange(
                            state.copy(
                                vehicleType = selectedType,
                                selectedBrandId = null,
                                selectedBrandName = "",
                                selectedModelId = null,
                                selectedModelName = "",
                                doors = if (selectedType == VehicleTypeUi.Car) {
                                    state.doors
                                } else {
                                    ""
                                },
                            )
                        )
                    },
                )

                VehicleTextField(
                    value = state.name,
                    onValueChange = { value: String -> onStateChange(state.copy(name = value)) },
                    label = stringResource(id = R.string.vehicle_form_name_label),
                    error = errors.name,
                )
                ExposedSelectorField(
                    label = stringResource(id = R.string.vehicle_form_brand_label),
                    selectedValue = state.selectedBrandName,
                    placeholder = stringResource(id = R.string.vehicle_form_placeholder_choose_brand),
                    options = brandOptions,
                    error = errors.brand,
                    enabled = true,
                    onSelected = { brandName: String ->
                        val selectedBrand: BrandOption? =
                            brands.firstOrNull { brand: BrandOption -> brand.name == brandName }
                        onStateChange(
                            state.copy(
                                selectedBrandId = selectedBrand?.id,
                                selectedBrandName = selectedBrand?.name.orEmpty(),
                                selectedModelId = null,
                                selectedModelName = "",
                            )
                        )
                    },
                )
                if (brands.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.vehicle_form_no_brands_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                ExposedSelectorField(
                    label = stringResource(id = R.string.vehicle_form_model_label),
                    selectedValue = state.selectedModelName,
                    placeholder = if (state.selectedBrandId == null) {
                        stringResource(id = R.string.vehicle_form_placeholder_select_brand_first)
                    } else {
                        stringResource(id = R.string.vehicle_form_placeholder_choose_model)
                    },
                    options = modelOptions,
                    error = errors.model,
                    enabled = state.selectedBrandId != null,
                    onSelected = { modelName: String ->
                        val selectedModel: ModelOption? =
                            models.firstOrNull { model: ModelOption -> model.name == modelName }
                        onStateChange(
                            state.copy(
                                selectedModelId = selectedModel?.id,
                                selectedModelName = selectedModel?.name.orEmpty(),
                            )
                        )
                    },
                )
                VehicleTextField(
                    value = state.year,
                    onValueChange = { value: String -> onStateChange(state.copy(year = value)) },
                    label = stringResource(id = R.string.vehicle_form_year_label),
                    error = errors.year,
                    keyboardType = KeyboardType.Number,
                )
                VehicleTextField(
                    value = state.licensePlate,
                    onValueChange = { value: String ->
                        onStateChange(state.copy(licensePlate = value))
                    },
                    label = stringResource(id = R.string.vehicle_form_license_plate_label),
                )
                VehicleTextField(
                    value = state.binNumber,
                    onValueChange = { value: String ->
                        onStateChange(state.copy(binNumber = value))
                    },
                    label = stringResource(id = R.string.vehicle_form_bin_label),
                )
                VehicleTextField(
                    value = state.vin,
                    onValueChange = { value: String -> onStateChange(state.copy(vin = value)) },
                    label = stringResource(id = R.string.vehicle_form_vin_label),
                    error = errors.vin,
                )
                if (state.vehicleType == VehicleTypeUi.Car) {
                    VehicleTextField(
                        value = state.doors,
                        onValueChange = { value: String ->
                            onStateChange(state.copy(doors = value))
                        },
                        label = stringResource(id = R.string.vehicle_form_doors_label),
                        error = errors.doors,
                        keyboardType = KeyboardType.Number,
                    )
                }
                VehicleTextField(
                    value = state.currentOdometerKm,
                    onValueChange = { value: String ->
                        onStateChange(state.copy(currentOdometerKm = value))
                    },
                    label = currentOdometerLabel,
                    error = errors.currentOdometerKm,
                    keyboardType = KeyboardType.Number,
                )
                ExposedSelectorField(
                    label = stringResource(id = R.string.vehicle_form_fuel_type_label),
                    selectedValue = state.fuelType,
                    placeholder = stringResource(id = R.string.vehicle_form_placeholder_fuel),
                    options = fuelOptions,
                    onSelected = { value: String ->
                        onStateChange(state.copy(fuelType = value))
                    },
                )
                ExposedSelectorField(
                    label = stringResource(id = R.string.vehicle_form_transmission_type_label),
                    selectedValue = state.transmissionType,
                    placeholder = stringResource(id = R.string.vehicle_form_placeholder_transmission),
                    options = transmissionOptions,
                    onSelected = { value: String ->
                        onStateChange(state.copy(transmissionType = value))
                    },
                )
                VehicleTextField(
                    value = state.transmissionSubtype,
                    onValueChange = { value: String ->
                        onStateChange(state.copy(transmissionSubtype = value))
                    },
                    label = stringResource(id = R.string.vehicle_form_transmission_subtype_label),
                    error = null,
                )
                Text(
                    text = stringResource(id = R.string.vehicle_form_transmission_subtype_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                VehicleTextField(
                    value = state.color,
                    onValueChange = { value: String -> onStateChange(state.copy(color = value)) },
                    label = stringResource(id = R.string.vehicle_form_color_label),
                )
                VehicleTextField(
                    value = state.notes,
                    onValueChange = { value: String -> onStateChange(state.copy(notes = value)) },
                    label = stringResource(id = R.string.vehicle_form_notes_label),
                )
                Text(
                    text = stringResource(id = R.string.vehicle_odometer_reminder_section_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = state.odometerReminderEnabled,
                        onCheckedChange = { checked: Boolean ->
                            onStateChange(state.copy(odometerReminderEnabled = checked))
                        },
                    )
                    Text(
                        text = stringResource(id = R.string.vehicle_odometer_reminder_enable),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                VehicleTextField(
                    value = state.odometerReminderIntervalValue,
                    onValueChange = { value: String ->
                        onStateChange(state.copy(odometerReminderIntervalValue = value))
                    },
                    label = stringResource(id = R.string.vehicle_odometer_reminder_interval_value_label),
                    error = errors.odometerReminderIntervalValue,
                    keyboardType = KeyboardType.Number,
                    enabled = state.odometerReminderEnabled,
                )
                ExposedSelectorField(
                    label = stringResource(id = R.string.vehicle_odometer_reminder_interval_unit_label),
                    selectedValue = state.odometerReminderIntervalUnit.name,
                    placeholder = stringResource(id = R.string.vehicle_odometer_reminder_unit_days),
                    options = reminderIntervalUnitOptions.map { option: Pair<OdometerReminderIntervalUnitUi, String> ->
                        option.first.name to option.second
                    },
                    onSelected = { selected: String ->
                        val selectedUnit: OdometerReminderIntervalUnitUi =
                            OdometerReminderIntervalUnitUi.valueOf(selected)
                        onStateChange(state.copy(odometerReminderIntervalUnit = selectedUnit))
                    },
                    enabled = state.odometerReminderEnabled,
                )
            }
        },
        confirmButton = {
            Button(onClick = onSubmit) {
                Text(text = stringResource(id = R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun VehicleTypeSelector(
    selectedType: VehicleTypeUi,
    onSelected: (VehicleTypeUi) -> Unit,
): Unit {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedType == VehicleTypeUi.Car,
            onClick = { onSelected(VehicleTypeUi.Car) },
            label = { Text(text = stringResource(id = R.string.vehicle_form_type_car)) },
        )
        FilterChip(
            selected = selectedType == VehicleTypeUi.Motorcycle,
            onClick = { onSelected(VehicleTypeUi.Motorcycle) },
            label = { Text(text = stringResource(id = R.string.vehicle_form_type_motorcycle)) },
        )
    }
    Spacer(modifier = Modifier.size(2.dp))
}

@Composable
private fun VehicleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
): Unit {
    val isNumericField: Boolean = keyboardType == KeyboardType.Number
    val sanitizedValue: String = if (isNumericField) {
        value.filter { character: Char -> character.isDigit() }
    } else {
        value
    }
    OutlinedTextField(
        value = sanitizedValue,
        onValueChange = { newValue: String ->
            if (isNumericField) {
                onValueChange(newValue.filter { character: Char -> character.isDigit() })
            } else {
                onValueChange(newValue)
            }
        },
        label = { Text(text = label) },
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        modifier = Modifier.fillMaxWidth(),
    )
    if (error != null) {
        Text(
            text = error,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExposedSelectorField(
    label: String,
    selectedValue: String,
    placeholder: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    enabled: Boolean = true,
    error: String? = null,
): Unit {
    var expanded by remember { mutableStateOf(false) }
    val displayText: String = options.firstOrNull { it.first == selectedValue }?.second ?: selectedValue
    Column(
        modifier = if (enabled) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .fillMaxWidth()
                .alpha(0.6f)
        },
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { isExpanded: Boolean ->
                if (enabled) {
                    expanded = isExpanded
                }
            },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = displayText,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                label = { Text(text = label) },
                placeholder = { Text(text = placeholder) },
                isError = error != null,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option: Pair<String, String> ->
                    DropdownMenuItem(
                        text = { Text(text = option.second) },
                        onClick = {
                            onSelected(option.first)
                            expanded = false
                        },
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
}
