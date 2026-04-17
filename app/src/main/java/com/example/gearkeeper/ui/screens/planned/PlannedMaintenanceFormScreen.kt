package com.example.gearkeeper.ui.screens.planned

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.PlannedMaintenanceEditRow
import com.example.gearkeeper.data.local.PlannedMaintenanceRepository
import com.example.gearkeeper.data.local.ServiceTypeOption
import com.example.gearkeeper.data.local.ServiceTypeRepository
import com.example.gearkeeper.data.local.VehicleListItem
import com.example.gearkeeper.data.local.VehicleRepository
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.domain.planned.PlanLeadTimeUnit
import com.example.gearkeeper.domain.planned.PlannedMaintenanceLeadRules
import com.example.gearkeeper.domain.planned.PlannedScheduleMode
import com.example.gearkeeper.domain.preferences.DistanceUnit
import com.example.gearkeeper.ui.navigation.NavRoutes
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.matchesListSearch
import com.example.gearkeeper.ui.util.localizedDisplayName
import com.example.gearkeeper.ui.util.rememberPreferSpanishLocale
import com.example.gearkeeper.ui.util.showIsoDatePickerDialog
import com.example.gearkeeper.ui.util.todayIsoDate

private val longIdsSaverPlanned: Saver<List<Long>, String> = Saver(
    save = { ids: List<Long> -> ids.joinToString(",") },
    restore = { raw: String ->
        if (raw.isBlank()) {
            emptyList()
        } else {
            raw.split(',').mapNotNull { token: String -> token.toLongOrNull() }
        }
    },
)

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun PlannedMaintenanceFormScreen(
    vehicleId: Long,
    vehicleRepository: VehicleRepository,
    serviceTypeRepository: ServiceTypeRepository,
    plannedMaintenanceRepository: PlannedMaintenanceRepository,
    onNavigateToAddServiceType: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    editingPlannedMaintenanceId: Long? = null,
): Unit {
    val context = LocalContext.current
    val isEditing: Boolean = editingPlannedMaintenanceId != null
    val needsVehiclePicker: Boolean =
        vehicleId == NavRoutes.MAINTENANCE_VEHICLE_PICKER_ID && !isEditing
    val editKey: Long = editingPlannedMaintenanceId ?: 0L
    var serviceOptions: List<ServiceTypeOption> by remember { mutableStateOf(emptyList()) }
    var vehicleOptions: List<VehicleListItem> by remember { mutableStateOf(emptyList()) }
    var vehicleMenuExpanded: Boolean by remember { mutableStateOf(false) }
    var selectedVehicleIdStorage: Long by rememberSaveable(vehicleId, editKey) {
        mutableStateOf(
            when {
                isEditing -> -1L
                needsVehiclePicker -> -1L
                else -> vehicleId
            },
        )
    }
    var fixedVehicleSummary: String by remember { mutableStateOf("") }

    var title: String by rememberSaveable(vehicleId, editKey) { mutableStateOf("") }
    var scheduleByDate: Boolean by rememberSaveable(vehicleId, editKey) { mutableStateOf(false) }
    var targetDate: String by rememberSaveable(vehicleId, editKey) { mutableStateOf(todayIsoDate()) }
    var targetOdometerText: String by rememberSaveable(vehicleId, editKey) { mutableStateOf("") }
    var leadAmountText: String by rememberSaveable(vehicleId, editKey) { mutableStateOf("1") }
    val leadTimeUnitState: MutableState<PlanLeadTimeUnit> = rememberSaveable(
        vehicleId,
        editKey,
        init = { mutableStateOf(PlanLeadTimeUnit.DAYS) },
    )
    var leadTimeUnit: PlanLeadTimeUnit by leadTimeUnitState
    var leadUnitMenuExpanded: Boolean by remember { mutableStateOf(false) }
    var leadDistanceText: String by rememberSaveable(vehicleId, editKey) { mutableStateOf("") }
    val selectedServiceIdsState: MutableState<List<Long>> = rememberSaveable(
        vehicleId,
        editKey,
        stateSaver = longIdsSaverPlanned,
        init = { mutableStateOf(emptyList<Long>()) },
    )
    var selectedServiceIds: List<Long> by selectedServiceIdsState
    var serviceSearchQuery: String by rememberSaveable(vehicleId, editKey) { mutableStateOf("") }
    var formError: String? by remember { mutableStateOf(null) }
    val targetDateInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val suggestionScrollState = rememberScrollState()
    val distanceUnit: DistanceUnit = LocalDistanceUnit.current
    val preferSpanish: Boolean = rememberPreferSpanishLocale()
    val odometerTargetLabel: String = when (distanceUnit) {
        DistanceUnit.METRIC_KM -> stringResource(id = R.string.planned_target_odometer_km)
        DistanceUnit.IMPERIAL_MILES -> stringResource(id = R.string.planned_target_odometer_mi)
    }
    val odometerLeadLabel: String = when (distanceUnit) {
        DistanceUnit.METRIC_KM -> stringResource(id = R.string.planned_lead_odometer_km)
        DistanceUnit.IMPERIAL_MILES -> stringResource(id = R.string.planned_lead_odometer_mi)
    }

    val serviceSuggestions: List<ServiceTypeOption> = remember(
        serviceOptions,
        serviceSearchQuery,
        selectedServiceIds,
    ) {
        val pool: List<ServiceTypeOption> = serviceOptions.filter { option: ServiceTypeOption ->
            option.id !in selectedServiceIds
        }
        if (serviceSearchQuery.isBlank()) {
            pool.take(20)
        } else {
            pool.filter { option: ServiceTypeOption ->
                matchesListSearch(serviceSearchQuery, option.name) ||
                    (option.nameEs != null && matchesListSearch(serviceSearchQuery, option.nameEs))
            }
        }
    }

    val vehicleMenuLabel: String = remember(selectedVehicleIdStorage, vehicleOptions, needsVehiclePicker) {
        if (!needsVehiclePicker || selectedVehicleIdStorage < 0L) {
            ""
        } else {
            vehicleOptions.firstOrNull { it.id == selectedVehicleIdStorage }?.let { v: VehicleListItem ->
                "${v.name} — ${v.brandName} · ${v.modelName}"
            }.orEmpty()
        }
    }

    LaunchedEffect(vehicleId, selectedVehicleIdStorage, needsVehiclePicker, editingPlannedMaintenanceId) {
        vehicleOptions = vehicleRepository.getAllVehicles()
        serviceOptions = serviceTypeRepository.getAllOrderedByName()
        selectedServiceIds = selectedServiceIds.filter { id: Long ->
            serviceOptions.any { option: ServiceTypeOption -> option.id == id }
        }
        if (editingPlannedMaintenanceId != null) {
            return@LaunchedEffect
        }
        if (!needsVehiclePicker) {
            fixedVehicleSummary = vehicleOptions.firstOrNull { it.id == vehicleId }?.let { v: VehicleListItem ->
                "${v.name} — ${v.brandName} · ${v.modelName}"
            } ?: context.getString(R.string.maintenance_vehicle_number_fallback, vehicleId)
        }
        if (needsVehiclePicker &&
            selectedVehicleIdStorage >= 0L &&
            vehicleOptions.none { it.id == selectedVehicleIdStorage }
        ) {
            selectedVehicleIdStorage = -1L
        }
    }

    LaunchedEffect(editingPlannedMaintenanceId, distanceUnit) {
        val editId: Long = editingPlannedMaintenanceId ?: return@LaunchedEffect
        vehicleOptions = vehicleRepository.getAllVehicles()
        serviceOptions = serviceTypeRepository.getAllOrderedByName()
        val editRow: PlannedMaintenanceEditRow = plannedMaintenanceRepository.getPlannedForEdit(editId)
            ?: run {
                formError = context.getString(R.string.planned_error_load_edit_failed)
                return@LaunchedEffect
            }
        formError = null
        title = editRow.title
        selectedVehicleIdStorage = editRow.vehicleId
        scheduleByDate = editRow.scheduleMode == PlannedScheduleMode.DATE
        when (editRow.scheduleMode) {
            PlannedScheduleMode.DATE -> {
                targetDate = editRow.targetDate?.trim().orEmpty().ifBlank { todayIsoDate() }
                targetOdometerText = ""
                leadAmountText = editRow.leadDays.coerceAtLeast(0).toString()
                leadTimeUnit = PlanLeadTimeUnit.DAYS
                leadDistanceText = ""
            }
            PlannedScheduleMode.ODOMETER -> {
                targetDate = todayIsoDate()
                val targetKm: Int = editRow.targetOdometerKm ?: 0
                targetOdometerText = OdometerUnitConverter.storedKmToDisplayIntString(
                    km = targetKm,
                    unit = distanceUnit,
                )
                leadAmountText = "1"
                leadTimeUnit = PlanLeadTimeUnit.DAYS
                leadDistanceText = OdometerUnitConverter.storedKmToDisplayIntString(
                    km = editRow.leadOdometerKm.coerceAtLeast(0),
                    unit = distanceUnit,
                )
            }
        }
        selectedServiceIds = editRow.serviceTypeIds.filter { id: Long ->
            serviceOptions.any { option: ServiceTypeOption -> option.id == id }
        }
        fixedVehicleSummary = vehicleOptions.firstOrNull { it.id == editRow.vehicleId }
            ?.let { v: VehicleListItem ->
                "${v.name} — ${v.brandName} · ${v.modelName}"
            }
            ?: context.getString(R.string.maintenance_vehicle_number_fallback, editRow.vehicleId)
    }

    fun idToName(id: Long): String {
        val option: ServiceTypeOption? = serviceOptions.firstOrNull { it.id == id }
        return option?.localizedDisplayName(preferSpanish = preferSpanish)
            ?: context.getString(R.string.maintenance_service_id_fallback, id)
    }

    val effectiveVehicleId: Long? =
        when {
            isEditing -> selectedVehicleIdStorage.takeIf { it >= 0L }
            needsVehiclePicker -> selectedVehicleIdStorage.takeIf { it >= 0L }
            else -> vehicleId
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = ListScreenHorizontalPadding,
                vertical = ListScreenVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.planned_form_section_vehicle),
            style = MaterialTheme.typography.titleMedium,
        )
        if (needsVehiclePicker) {
            if (vehicleOptions.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.maintenance_add_vehicle_first),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = vehicleMenuExpanded,
                    onExpandedChange = { expanded: Boolean -> vehicleMenuExpanded = expanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        value = vehicleMenuLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = stringResource(id = R.string.maintenance_select_vehicle_label)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleMenuExpanded)
                        },
                    )
                    DropdownMenu(
                        expanded = vehicleMenuExpanded,
                        onDismissRequest = { vehicleMenuExpanded = false },
                    ) {
                        vehicleOptions.forEach { option: VehicleListItem ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${option.name} — ${option.brandName} · ${option.modelName}",
                                    )
                                },
                                onClick = {
                                    selectedVehicleIdStorage = option.id
                                    vehicleMenuExpanded = false
                                    formError = null
                                },
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = fixedVehicleSummary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                formError = null
            },
            label = { Text(text = stringResource(id = R.string.maintenance_title_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(id = R.string.planned_schedule_mode_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = scheduleByDate,
                onClick = {
                    scheduleByDate = true
                    formError = null
                },
                label = { Text(text = stringResource(id = R.string.planned_schedule_by_date)) },
            )
            FilterChip(
                selected = !scheduleByDate,
                onClick = {
                    scheduleByDate = false
                    formError = null
                },
                label = { Text(text = stringResource(id = R.string.planned_schedule_by_odometer)) },
            )
        }

        if (scheduleByDate) {
            OutlinedTextField(
                value = targetDate,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.planned_target_date_label)) },
                singleLine = true,
                interactionSource = targetDateInteractionSource,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showIsoDatePickerDialog(
                                context = context,
                                initialIsoDate = targetDate,
                            ) { picked: String ->
                                targetDate = picked
                                formError = null
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = stringResource(id = R.string.cd_pick_date),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = targetDateInteractionSource,
                        indication = null,
                    ) {
                        showIsoDatePickerDialog(
                            context = context,
                            initialIsoDate = targetDate,
                        ) { picked: String ->
                            targetDate = picked
                            formError = null
                        }
                    },
            )
            Text(
                text = stringResource(id = R.string.date_picker_tap_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.planned_lead_time_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(id = R.string.planned_lead_time_supporting),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = leadAmountText,
                    onValueChange = { value: String ->
                        leadAmountText = value.filter { ch: Char -> ch.isDigit() }
                        formError = null
                    },
                    label = { Text(text = stringResource(id = R.string.planned_lead_time_value_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                val unitLabel: String = when (leadTimeUnit) {
                    PlanLeadTimeUnit.DAYS -> stringResource(id = R.string.planned_lead_unit_days)
                    PlanLeadTimeUnit.WEEKS -> stringResource(id = R.string.planned_lead_unit_weeks)
                    PlanLeadTimeUnit.MONTHS -> stringResource(id = R.string.planned_lead_unit_months)
                }
                ExposedDropdownMenuBox(
                    expanded = leadUnitMenuExpanded,
                    onExpandedChange = { expanded: Boolean -> leadUnitMenuExpanded = expanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        value = unitLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = stringResource(id = R.string.planned_lead_unit_label)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = leadUnitMenuExpanded)
                        },
                    )
                    DropdownMenu(
                        expanded = leadUnitMenuExpanded,
                        onDismissRequest = { leadUnitMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.planned_lead_unit_days)) },
                            onClick = {
                                leadTimeUnit = PlanLeadTimeUnit.DAYS
                                leadUnitMenuExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.planned_lead_unit_weeks)) },
                            onClick = {
                                leadTimeUnit = PlanLeadTimeUnit.WEEKS
                                leadUnitMenuExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.planned_lead_unit_months)) },
                            onClick = {
                                leadTimeUnit = PlanLeadTimeUnit.MONTHS
                                leadUnitMenuExpanded = false
                            },
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = targetOdometerText,
                onValueChange = { value: String ->
                    targetOdometerText = value.filter { ch: Char -> ch.isDigit() }
                    formError = null
                },
                label = { Text(text = odometerTargetLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Text(
                text = stringResource(id = R.string.planned_lead_distance_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(id = R.string.planned_lead_distance_supporting),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = leadDistanceText,
                onValueChange = { value: String ->
                    leadDistanceText = value.filter { ch: Char -> ch.isDigit() }
                    formError = null
                },
                label = { Text(text = odometerLeadLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        Text(
            text = stringResource(id = R.string.maintenance_services_performed_title),
            style = MaterialTheme.typography.titleMedium,
        )
        if (selectedServiceIds.isEmpty()) {
            Text(
                text = stringResource(id = R.string.maintenance_tap_service_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                selectedServiceIds.forEach { serviceId: Long ->
                    InputChip(
                        selected = true,
                        onClick = { },
                        label = {
                            Text(
                                text = idToName(serviceId),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(id = R.string.cd_remove_service_from_visit),
                                modifier = Modifier
                                    .size(size = 18.dp)
                                    .clickable(
                                        onClick = {
                                            selectedServiceIds =
                                                selectedServiceIds.filter { it != serviceId }
                                        },
                                    ),
                            )
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = serviceSearchQuery,
            onValueChange = { value: String ->
                serviceSearchQuery = value
                formError = null
            },
            label = { Text(text = stringResource(id = R.string.maintenance_search_catalog_label)) },
            placeholder = { Text(text = stringResource(id = R.string.maintenance_search_catalog_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        when {
            serviceOptions.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.maintenance_no_services_in_catalog),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 176.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(state = suggestionScrollState),
                    ) {
                        if (serviceSuggestions.isEmpty()) {
                            Text(
                                text = if (serviceOptions.all { option: ServiceTypeOption ->
                                        option.id in selectedServiceIds
                                    }
                                ) {
                                    stringResource(id = R.string.maintenance_all_services_already_added)
                                } else {
                                    stringResource(id = R.string.maintenance_no_service_matches)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                            )
                        } else {
                            serviceSuggestions.forEachIndexed { index: Int, option: ServiceTypeOption ->
                                if (index > 0) {
                                    HorizontalDivider()
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!selectedServiceIds.contains(option.id)) {
                                                selectedServiceIds = selectedServiceIds + option.id
                                            }
                                            serviceSearchQuery = ""
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = option.localizedDisplayName(preferSpanish = preferSpanish),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        TextButton(
            onClick = onNavigateToAddServiceType,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.maintenance_create_service_type))
        }

        if (formError != null) {
            Text(
                text = formError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = {
                val vid: Long = effectiveVehicleId ?: run {
                    formError = context.getString(R.string.maintenance_error_select_vehicle)
                    return@Button
                }
                if (title.isBlank()) {
                    formError = context.getString(R.string.maintenance_error_title_required)
                    return@Button
                }
                if (selectedServiceIds.isEmpty()) {
                    formError = context.getString(R.string.maintenance_error_services_required)
                    return@Button
                }
                val mode: PlannedScheduleMode = if (scheduleByDate) {
                    PlannedScheduleMode.DATE
                } else {
                    PlannedScheduleMode.ODOMETER
                }
                val leadDaysStored: Int
                val leadOdometerStored: Int
                val targetDateStored: String?
                val targetOdoStored: Int?
                when (mode) {
                    PlannedScheduleMode.DATE -> {
                        if (targetDate.isBlank()) {
                            formError = context.getString(R.string.planned_error_target_date_required)
                            return@Button
                        }
                        val leadAmt: Int = leadAmountText.toIntOrNull() ?: run {
                            formError = context.getString(R.string.planned_error_lead_number)
                            return@Button
                        }
                        if (leadAmt < 0) {
                            formError = context.getString(R.string.planned_error_lead_negative)
                            return@Button
                        }
                        leadDaysStored = PlannedMaintenanceLeadRules.leadTimeUnitToDays(
                            value = leadAmt,
                            unit = leadTimeUnit,
                        )
                        leadOdometerStored = 0
                        targetDateStored = targetDate.trim()
                        targetOdoStored = null
                    }
                    PlannedScheduleMode.ODOMETER -> {
                        if (targetOdometerText.isBlank()) {
                            formError = context.getString(R.string.planned_error_target_odometer_required)
                            return@Button
                        }
                        if (leadDistanceText.isBlank()) {
                            formError = context.getString(R.string.planned_error_lead_odometer_required)
                            return@Button
                        }
                        val rawTarget: Int = targetOdometerText.toIntOrNull() ?: run {
                            formError = context.getString(R.string.maintenance_error_odometer_invalid)
                            return@Button
                        }
                        val rawLead: Int = leadDistanceText.toIntOrNull() ?: run {
                            formError = context.getString(R.string.maintenance_error_odometer_invalid)
                            return@Button
                        }
                        if (rawTarget < 0 || rawLead < 0) {
                            formError = context.getString(R.string.maintenance_error_odometer_negative)
                            return@Button
                        }
                        val targetKm: Int = OdometerUnitConverter.userInputToStoredKm(
                            raw = rawTarget,
                            unit = distanceUnit,
                        )
                        val leadKm: Int = OdometerUnitConverter.userInputToStoredKm(
                            raw = rawLead,
                            unit = distanceUnit,
                        )
                        if (leadKm >= targetKm) {
                            formError = context.getString(R.string.planned_error_lead_must_be_less_than_target)
                            return@Button
                        }
                        leadDaysStored = 0
                        leadOdometerStored = leadKm
                        targetDateStored = null
                        targetOdoStored = targetKm
                    }
                }
                val editId: Long? = editingPlannedMaintenanceId
                if (editId != null) {
                    val ok: Boolean = plannedMaintenanceRepository.updatePlanned(
                        plannedId = editId,
                        vehicleId = vid,
                        title = title,
                        scheduleMode = mode,
                        targetDate = targetDateStored,
                        targetOdometerKm = targetOdoStored,
                        leadDays = leadDaysStored,
                        leadOdometerKm = leadOdometerStored,
                        serviceTypeIds = selectedServiceIds,
                    )
                    if (!ok) {
                        formError = context.getString(R.string.planned_error_save_failed)
                    } else {
                        onSaved()
                    }
                } else {
                    val newId: Long? = plannedMaintenanceRepository.insertPlanned(
                        vehicleId = vid,
                        title = title,
                        scheduleMode = mode,
                        targetDate = targetDateStored,
                        targetOdometerKm = targetOdoStored,
                        leadDays = leadDaysStored,
                        leadOdometerKm = leadOdometerStored,
                        serviceTypeIds = selectedServiceIds,
                    )
                    if (newId == null) {
                        formError = context.getString(R.string.planned_error_save_failed)
                    } else {
                        onSaved()
                    }
                }
            },
        ) {
            Text(text = stringResource(id = R.string.planned_save_button))
        }
    }
}
