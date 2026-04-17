package com.example.gearkeeper.ui.screens.services

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import com.example.gearkeeper.R
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.domain.preferences.DistanceUnit
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.gearkeeper.data.local.MaintenanceRepository
import com.example.gearkeeper.data.local.ReplacedPartHistoryPick
import com.example.gearkeeper.data.local.ReplacedPartInput
import com.example.gearkeeper.data.local.ReplacedPartRepository
import com.example.gearkeeper.data.local.ServiceTypeOption
import com.example.gearkeeper.data.local.ServiceTypeRepository
import com.example.gearkeeper.data.local.OdometerRepository
import com.example.gearkeeper.data.local.VehicleListItem
import com.example.gearkeeper.data.local.VehicleRepository
import com.example.gearkeeper.ui.navigation.NavRoutes
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.matchesListSearch
import com.example.gearkeeper.ui.util.localizedDisplayName
import com.example.gearkeeper.ui.util.rememberPreferSpanishLocale
import com.example.gearkeeper.ui.util.showIsoDatePickerDialog
import com.example.gearkeeper.ui.util.todayIsoDate
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

/** Yellow warning callout (not destructive error). */
private val OdometerWarningSurface: Color = Color(0xFFFFF9C4)
private val OdometerWarningContent: Color = Color(0xFF5C4A00)
private val OdometerWarningIcon: Color = Color(0xFFF57F17)

private data class ReplacedPartLine(
    val localKey: String,
    val sourceHistoryRowId: Long?,
    val title: String,
    val partOrSerial: String,
    val brand: String,
    val store: String,
    val notes: String,
) {
    fun toInput(): ReplacedPartInput = ReplacedPartInput(
        title = title.trim(),
        partOrSerial = partOrSerial.trim(),
        brand = brand.takeIf { it.isNotBlank() },
        store = store.takeIf { it.isNotBlank() },
        notes = notes.takeIf { it.isNotBlank() },
    )

    companion object {
        fun fromHistoryPick(pick: ReplacedPartHistoryPick): ReplacedPartLine = ReplacedPartLine(
            localKey = UUID.randomUUID().toString(),
            sourceHistoryRowId = pick.id,
            title = pick.title,
            partOrSerial = pick.partOrSerial,
            brand = pick.brand.orEmpty(),
            store = pick.store.orEmpty(),
            notes = pick.notes.orEmpty(),
        )

        fun fromSavedInput(input: ReplacedPartInput): ReplacedPartLine = ReplacedPartLine(
            localKey = UUID.randomUUID().toString(),
            sourceHistoryRowId = null,
            title = input.title,
            partOrSerial = input.partOrSerial,
            brand = input.brand.orEmpty(),
            store = input.store.orEmpty(),
            notes = input.notes.orEmpty(),
        )
    }
}

private val replacedPartLineListSaver: Saver<List<ReplacedPartLine>, String> = Saver(
    save = { lines: List<ReplacedPartLine> ->
        val ja = JSONArray()
        for (l: ReplacedPartLine in lines) {
            ja.put(
                JSONObject().apply {
                    put("k", l.localKey)
                    if (l.sourceHistoryRowId != null) {
                        put("h", l.sourceHistoryRowId)
                    } else {
                        put("h", JSONObject.NULL)
                    }
                    put("t", l.title)
                    put("p", l.partOrSerial)
                    put("b", l.brand)
                    put("s", l.store)
                    put("n", l.notes)
                },
            )
        }
        ja.toString()
    },
    restore = { raw: String ->
        try {
            val ja = JSONArray(raw)
            buildList {
                for (i: Int in 0 until ja.length()) {
                    val o: JSONObject = ja.getJSONObject(i)
                    val historyId: Long? = if (o.isNull("h")) {
                        null
                    } else {
                        o.getLong("h")
                    }
                    add(
                        ReplacedPartLine(
                            localKey = o.optString("k", UUID.randomUUID().toString()),
                            sourceHistoryRowId = historyId,
                            title = o.optString("t", ""),
                            partOrSerial = o.optString("p", ""),
                            brand = o.optString("b", ""),
                            store = o.optString("s", ""),
                            notes = o.optString("n", ""),
                        ),
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    },
)

private val longIdsSaver: Saver<List<Long>, String> = Saver(
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
fun MaintenanceFormScreen(
    vehicleId: Long,
    presetServiceTypeId: Long,
    vehicleRepository: VehicleRepository,
    serviceTypeRepository: ServiceTypeRepository,
    maintenanceRepository: MaintenanceRepository,
    replacedPartRepository: ReplacedPartRepository,
    odometerRepository: OdometerRepository,
    onNavigateToAddServiceType: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    editingMaintenanceId: Long? = null,
): Unit {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val needsVehiclePicker: Boolean =
        vehicleId == NavRoutes.MAINTENANCE_VEHICLE_PICKER_ID && editingMaintenanceId == null
    var serviceOptions: List<ServiceTypeOption> by remember { mutableStateOf(emptyList()) }
    var vehicleOptions: List<VehicleListItem> by remember { mutableStateOf(emptyList()) }
    var refreshTick: Int by remember { mutableStateOf(0) }

    var vehicleMenuExpanded: Boolean by remember { mutableStateOf(false) }
    var selectedVehicleIdStorage: Long by rememberSaveable(
        vehicleId,
        presetServiceTypeId,
        editingMaintenanceId ?: -2L,
    ) {
        mutableStateOf(
            if (vehicleId == NavRoutes.MAINTENANCE_VEHICLE_PICKER_ID) {
                -1L
            } else {
                vehicleId
            },
        )
    }
    var fixedVehicleSummary: String by remember { mutableStateOf("") }

    var title: String by rememberSaveable(vehicleId, presetServiceTypeId, editingMaintenanceId ?: -2L) {
        mutableStateOf("")
    }
    var performedDate: String by rememberSaveable(vehicleId, presetServiceTypeId, editingMaintenanceId ?: -2L) {
        mutableStateOf(todayIsoDate())
    }
    var odometer: String by rememberSaveable(vehicleId, presetServiceTypeId, editingMaintenanceId ?: -2L) {
        mutableStateOf("")
    }
    var addToOdometerRegister: Boolean by rememberSaveable(vehicleId, presetServiceTypeId, editingMaintenanceId ?: -2L) {
        mutableStateOf(true)
    }
    var notesHead: String by rememberSaveable(vehicleId, presetServiceTypeId, editingMaintenanceId ?: -2L) {
        mutableStateOf("")
    }
    val selectedServiceIdsState: MutableState<List<Long>> = rememberSaveable(
        vehicleId,
        presetServiceTypeId,
        editingMaintenanceId ?: -2L,
        stateSaver = longIdsSaver,
        init = { mutableStateOf(emptyList<Long>()) },
    )
    var selectedServiceIds: List<Long> by selectedServiceIdsState
    var serviceSearchQuery: String by rememberSaveable(vehicleId, presetServiceTypeId, editingMaintenanceId ?: -2L) {
        mutableStateOf("")
    }
    val replacedPartLinesState: MutableState<List<ReplacedPartLine>> = rememberSaveable(
        vehicleId,
        presetServiceTypeId,
        editingMaintenanceId ?: -2L,
        stateSaver = replacedPartLineListSaver,
        init = { mutableStateOf(emptyList<ReplacedPartLine>()) },
    )
    var replacedPartLines: List<ReplacedPartLine> by replacedPartLinesState
    var partSearchQuery: String by rememberSaveable(vehicleId, presetServiceTypeId, editingMaintenanceId ?: -2L) {
        mutableStateOf("")
    }
    var partSuggestions: List<ReplacedPartHistoryPick> by remember { mutableStateOf(emptyList()) }
    var manualPartDialogOpen: Boolean by remember { mutableStateOf(false) }
    var manualPartTitle: String by remember { mutableStateOf("") }
    var manualPartSerial: String by remember { mutableStateOf("") }
    var manualPartBrand: String by remember { mutableStateOf("") }
    var manualPartStore: String by remember { mutableStateOf("") }
    var manualPartNotes: String by remember { mutableStateOf("") }
    var manualPartError: String? by remember { mutableStateOf(null) }
    var loadedEditVehicleId: Long? by remember { mutableStateOf(null) }
    var formError: String? by remember { mutableStateOf(null) }
    val performedDateInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    /** Latest odometer in km from register / vehicle; used to reject lower readings. */
    var lastRecordedOdometerKm: Int? by remember { mutableStateOf(null) }
    val suggestionScrollState = rememberScrollState()
    val partHistoryScrollState = rememberScrollState()
    val distanceUnit: DistanceUnit = LocalDistanceUnit.current
    val odometerFieldLabel: String = when (distanceUnit) {
        DistanceUnit.METRIC_KM -> stringResource(id = R.string.odometer_label_km)
        DistanceUnit.IMPERIAL_MILES -> stringResource(id = R.string.odometer_label_mi)
    }
    val preferSpanish: Boolean = rememberPreferSpanishLocale()
    val emptyPartChipLabel: String = stringResource(id = R.string.maintenance_replaced_parts_empty_chip)

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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTick += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val effectiveVehicleId: Long? = when {
        needsVehiclePicker -> selectedVehicleIdStorage.takeIf { it >= 0L }
        editingMaintenanceId != null -> loadedEditVehicleId
        else -> vehicleId
    }
    val vehicleMenuLabel: String = remember(effectiveVehicleId, vehicleOptions, needsVehiclePicker) {
        if (!needsVehiclePicker || effectiveVehicleId == null) {
            ""
        } else {
            vehicleOptions.firstOrNull { it.id == effectiveVehicleId }?.let { v: VehicleListItem ->
                "${v.name} — ${v.brandName} · ${v.modelName}"
            }.orEmpty()
        }
    }

    LaunchedEffect(editingMaintenanceId) {
        if (editingMaintenanceId == null) {
            loadedEditVehicleId = null
            return@LaunchedEffect
        }
        val load = maintenanceRepository.getMaintenanceEditLoad(maintenanceId = editingMaintenanceId) ?: return@LaunchedEffect
        loadedEditVehicleId = load.vehicleId
        title = load.title
        performedDate = load.performedDate
        odometer = load.odometerKm?.let { km: Int ->
            OdometerUnitConverter.storedKmToDisplayIntString(km = km, unit = distanceUnit)
        }.orEmpty()
        notesHead = load.notes.orEmpty()
        selectedServiceIds = load.serviceTypeIds
        replacedPartLines = load.replacedParts.map { input: ReplacedPartInput ->
            ReplacedPartLine.fromSavedInput(input = input)
        }
        addToOdometerRegister = maintenanceRepository.hasOdometerReadingLinked(maintenanceId = editingMaintenanceId)
    }

    LaunchedEffect(
        refreshTick,
        vehicleId,
        effectiveVehicleId,
        needsVehiclePicker,
        editingMaintenanceId,
        loadedEditVehicleId,
    ) {
        vehicleOptions = vehicleRepository.getAllVehicles()
        if (!needsVehiclePicker) {
            when {
                editingMaintenanceId != null -> {
                    val summaryVehicleId: Long? = loadedEditVehicleId
                    if (summaryVehicleId != null) {
                        fixedVehicleSummary = vehicleOptions.firstOrNull { it.id == summaryVehicleId }
                            ?.let { v: VehicleListItem ->
                                "${v.name} — ${v.brandName} · ${v.modelName}"
                            }
                            ?: context.getString(
                                R.string.maintenance_vehicle_number_fallback,
                                summaryVehicleId,
                            )
                    }
                }
                else -> {
                    fixedVehicleSummary = vehicleOptions.firstOrNull { it.id == vehicleId }?.let { v: VehicleListItem ->
                        "${v.name} — ${v.brandName} · ${v.modelName}"
                    } ?: context.getString(R.string.maintenance_vehicle_number_fallback, vehicleId)
                }
            }
        }
        val vehicleIdForOdometer: Long? = when {
            needsVehiclePicker -> selectedVehicleIdStorage.takeIf { it >= 0L }
            editingMaintenanceId != null -> loadedEditVehicleId
            else -> vehicleId
        }
        lastRecordedOdometerKm = vehicleIdForOdometer?.let { id: Long ->
            val mid: Long? = editingMaintenanceId
            if (mid != null) {
                odometerRepository.getLatestOdometerKmForVehicleExcludingMaintenance(
                    vehicleId = id,
                    excludeMaintenanceId = mid,
                )
            } else {
                vehicleRepository.getVehicleListItem(vehicleId = id)?.lastOdometerKm
            }
        }
        serviceOptions = serviceTypeRepository.getAllOrderedByName()
        selectedServiceIds = selectedServiceIds.filter { id: Long ->
            serviceOptions.any { option: ServiceTypeOption -> option.id == id }
        }
        if (needsVehiclePicker &&
            effectiveVehicleId != null &&
            vehicleOptions.none { it.id == effectiveVehicleId }
        ) {
            selectedVehicleIdStorage = -1L
        }
    }

    LaunchedEffect(partSearchQuery, effectiveVehicleId, replacedPartLines, editingMaintenanceId) {
        val vid: Long = effectiveVehicleId ?: run {
            partSuggestions = emptyList()
            return@LaunchedEffect
        }
        partSuggestions = replacedPartRepository.searchReplacedPartHistory(
            vehicleId = vid,
            rawQuery = partSearchQuery,
            excludeRowIds = replacedPartLines.mapNotNull { line: ReplacedPartLine -> line.sourceHistoryRowId }.toSet(),
        )
    }

    LaunchedEffect(presetServiceTypeId, serviceOptions) {
        if (presetServiceTypeId > 0L && serviceOptions.any { it.id == presetServiceTypeId }) {
            if (!selectedServiceIds.contains(presetServiceTypeId)) {
                selectedServiceIds = selectedServiceIds + presetServiceTypeId
            }
        }
    }

    fun idToName(id: Long): String {
        val option: ServiceTypeOption? = serviceOptions.firstOrNull { it.id == id }
        return option?.localizedDisplayName(preferSpanish = preferSpanish)
            ?: context.getString(R.string.maintenance_service_id_fallback, id)
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
            text = stringResource(id = R.string.maintenance_vehicle_section_title),
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
        OutlinedTextField(
            value = performedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(id = R.string.maintenance_date_label)) },
            singleLine = true,
            interactionSource = performedDateInteractionSource,
            trailingIcon = {
                IconButton(
                    onClick = {
                        showIsoDatePickerDialog(
                            context = context,
                            initialIsoDate = performedDate,
                        ) { picked: String ->
                            performedDate = picked
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
                    interactionSource = performedDateInteractionSource,
                    indication = null,
                ) {
                    showIsoDatePickerDialog(
                        context = context,
                        initialIsoDate = performedDate,
                    ) { picked: String ->
                        performedDate = picked
                        formError = null
                    }
                },
        )
        Text(
            text = stringResource(id = R.string.date_picker_tap_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = odometer,
            onValueChange = { value: String ->
                odometer = value.filter { character: Char -> character.isDigit() }
                formError = null
            },
            label = { Text(text = odometerFieldLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Text(
            text = stringResource(id = R.string.maintenance_update_odometer_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 12.dp),
            color = OdometerWarningSurface,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(space = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = OdometerWarningIcon,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.maintenance_odometer_warning_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = OdometerWarningContent,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = addToOdometerRegister,
                        onCheckedChange = { checked: Boolean -> addToOdometerRegister = checked },
                        colors = CheckboxDefaults.colors(
                            checkedColor = OdometerWarningIcon,
                            uncheckedColor = OdometerWarningContent.copy(alpha = 0.5f),
                            checkmarkColor = Color.White,
                        ),
                    )
                    Text(
                        text = stringResource(id = R.string.maintenance_save_odometer_register),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = OdometerWarningContent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                addToOdometerRegister = !addToOdometerRegister
                            },
                    )
                }
            }
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
            trailingIcon = {
                if (serviceSearchQuery.isNotBlank()) {
                    IconButton(
                        onClick = {
                            serviceSearchQuery = ""
                            formError = null
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.action_clear_text),
                        )
                    }
                }
            },
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
                if (serviceSuggestions.size > 5) {
                    Text(
                        text = stringResource(id = R.string.maintenance_scroll_list_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        TextButton(
            onClick = onNavigateToAddServiceType,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.maintenance_create_service_type))
        }
        Text(
            text = stringResource(id = R.string.maintenance_replaced_parts_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(id = R.string.maintenance_parts_search_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (replacedPartLines.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                replacedPartLines.forEach { line: ReplacedPartLine ->
                    val chipLabel: String = line.title.trim().ifBlank { line.partOrSerial.trim() }
                        .ifBlank { emptyPartChipLabel }
                    InputChip(
                        selected = true,
                        onClick = { },
                        label = {
                            Text(
                                text = chipLabel.take(n = 48),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(id = R.string.maintenance_replaced_parts_remove_row),
                                modifier = Modifier
                                    .size(size = 18.dp)
                                    .clickable(
                                        onClick = {
                                            replacedPartLines = replacedPartLines.filter { it.localKey != line.localKey }
                                        },
                                    ),
                            )
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = partSearchQuery,
            onValueChange = { value: String ->
                partSearchQuery = value
                formError = null
            },
            label = { Text(text = stringResource(id = R.string.maintenance_parts_search_label)) },
            placeholder = { Text(text = stringResource(id = R.string.maintenance_search_catalog_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        when {
            effectiveVehicleId == null -> {
                Text(
                    text = stringResource(id = R.string.maintenance_parts_vehicle_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(state = partHistoryScrollState),
                    ) {
                        if (partSearchQuery.isBlank()) {
                            Text(
                                text = stringResource(id = R.string.maintenance_parts_type_to_search),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                            )
                        } else if (partSuggestions.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.maintenance_no_service_matches),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                            )
                        } else {
                            partSuggestions.forEachIndexed { index: Int, pick: ReplacedPartHistoryPick ->
                                if (index > 0) {
                                    HorizontalDivider()
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            replacedPartLines = replacedPartLines + ReplacedPartLine.fromHistoryPick(
                                                pick = pick,
                                            )
                                            partSearchQuery = ""
                                            formError = null
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pick.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        val sub: String = listOfNotNull(
                                            pick.partOrSerial.takeIf { it.isNotBlank() },
                                            pick.brand?.takeIf { it.isNotBlank() },
                                            pick.store?.takeIf { it.isNotBlank() },
                                        ).joinToString(separator = " · ")
                                        if (sub.isNotBlank()) {
                                            Text(
                                                text = sub,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        TextButton(
            onClick = {
                manualPartTitle = ""
                manualPartSerial = ""
                manualPartBrand = ""
                manualPartStore = ""
                manualPartNotes = ""
                manualPartError = null
                manualPartDialogOpen = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.maintenance_parts_add_manual))
        }
        OutlinedTextField(
            value = notesHead,
            onValueChange = {
                notesHead = it
                formError = null
            },
            label = { Text(text = stringResource(id = R.string.maintenance_notes_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
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
                if (performedDate.isBlank()) {
                    formError = context.getString(R.string.maintenance_error_date_required)
                    return@Button
                }
                if (selectedServiceIds.isEmpty()) {
                    formError = context.getString(R.string.maintenance_error_services_required)
                    return@Button
                }
                if (odometer.isBlank()) {
                    formError = context.getString(R.string.maintenance_error_odometer_required)
                    return@Button
                }
                val odoInt: Int = odometer.trim().toIntOrNull() ?: run {
                    formError = context.getString(R.string.maintenance_error_odometer_invalid)
                    return@Button
                }
                if (odoInt < 0) {
                    formError = context.getString(R.string.maintenance_error_odometer_negative)
                    return@Button
                }
                val odometerKm: Int = OdometerUnitConverter.userInputToStoredKm(
                    raw = odoInt,
                    unit = distanceUnit,
                )
                val previousKm: Int? = lastRecordedOdometerKm
                if (previousKm != null && odometerKm < previousKm) {
                    val formattedPrevious: String =
                        OdometerUnitConverter.formatStoredKm(km = previousKm, unit = distanceUnit)
                    formError = context.getString(
                        R.string.maintenance_error_odometer_below_previous,
                        formattedPrevious,
                    )
                    return@Button
                }
                val replacedParts: List<ReplacedPartInput> = replacedPartLines
                    .map { line: ReplacedPartLine -> line.toInput() }
                    .filter { input: ReplacedPartInput -> input.title.isNotBlank() }
                val editId: Long? = editingMaintenanceId
                if (editId != null) {
                    val ok: Boolean = maintenanceRepository.updateMaintenance(
                        maintenanceId = editId,
                        vehicleId = vid,
                        title = title,
                        performedDate = performedDate,
                        odometerKm = odometerKm,
                        notes = notesHead.trim().takeIf { it.isNotEmpty() },
                        serviceTypeIds = selectedServiceIds,
                        addOdometerReading = addToOdometerRegister,
                        replacedParts = replacedParts,
                    )
                    if (!ok) {
                        formError = context.getString(R.string.maintenance_error_save_failed)
                    } else {
                        onSaved()
                    }
                } else {
                    val newId: Long? = maintenanceRepository.insertMaintenance(
                        vehicleId = vid,
                        title = title,
                        performedDate = performedDate,
                        odometerKm = odometerKm,
                        notes = notesHead.trim().takeIf { it.isNotEmpty() },
                        serviceTypeIds = selectedServiceIds,
                        addOdometerReading = addToOdometerRegister,
                        replacedParts = replacedParts,
                    )
                    if (newId == null) {
                        formError = context.getString(R.string.maintenance_error_save_failed)
                    } else {
                        onSaved()
                    }
                }
            },
        ) {
            Text(
                text = stringResource(
                    id = if (editingMaintenanceId != null) {
                        R.string.maintenance_save_changes
                    } else {
                        R.string.maintenance_save_button
                    },
                ),
            )
        }
    }

    if (manualPartDialogOpen) {
        AlertDialog(
            onDismissRequest = { manualPartDialogOpen = false },
            title = { Text(text = stringResource(id = R.string.maintenance_parts_dialog_add_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (manualPartError != null) {
                        Text(
                            text = manualPartError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    OutlinedTextField(
                        value = manualPartTitle,
                        onValueChange = {
                            manualPartTitle = it
                            manualPartError = null
                        },
                        label = { Text(text = stringResource(id = R.string.replaced_parts_draft_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = manualPartSerial,
                        onValueChange = { manualPartSerial = it },
                        label = { Text(text = stringResource(id = R.string.replaced_parts_draft_part_serial)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = manualPartBrand,
                        onValueChange = { manualPartBrand = it },
                        label = { Text(text = stringResource(id = R.string.replaced_parts_draft_brand)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = manualPartStore,
                        onValueChange = { manualPartStore = it },
                        label = { Text(text = stringResource(id = R.string.replaced_parts_draft_store)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = manualPartNotes,
                        onValueChange = { manualPartNotes = it },
                        label = { Text(text = stringResource(id = R.string.replaced_parts_draft_notes)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualPartTitle.isBlank()) {
                            manualPartError = context.getString(R.string.maintenance_error_title_required)
                            return@TextButton
                        }
                        replacedPartLines = replacedPartLines + ReplacedPartLine(
                            localKey = UUID.randomUUID().toString(),
                            sourceHistoryRowId = null,
                            title = manualPartTitle,
                            partOrSerial = manualPartSerial,
                            brand = manualPartBrand,
                            store = manualPartStore,
                            notes = manualPartNotes,
                        )
                        manualPartDialogOpen = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.replaced_parts_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { manualPartDialogOpen = false }) {
                    Text(text = stringResource(id = R.string.replaced_parts_dialog_cancel))
                }
            },
        )
    }
}
