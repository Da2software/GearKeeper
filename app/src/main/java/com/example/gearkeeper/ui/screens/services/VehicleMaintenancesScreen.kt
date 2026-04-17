package com.example.gearkeeper.ui.screens.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import com.example.gearkeeper.R
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.domain.preferences.DistanceUnit
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.gearkeeper.data.local.MaintenanceRepository
import com.example.gearkeeper.data.local.MaintenanceSummary
import com.example.gearkeeper.data.local.OdometerReading
import com.example.gearkeeper.data.local.OdometerRepository
import com.example.gearkeeper.data.local.VehicleListItem
import com.example.gearkeeper.data.local.VehicleRepository
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionEmptyState
import com.example.gearkeeper.ui.patterns.SectionListHeader
import com.example.gearkeeper.ui.screens.vehicles.vehicleOdometerSubtitle
import com.example.gearkeeper.ui.util.rememberPreferSpanishLocale
import com.example.gearkeeper.ui.util.todayIsoDate

@Composable
fun VehicleMaintenancesScreen(
    vehicleId: Long,
    vehicleRepository: VehicleRepository,
    maintenanceRepository: MaintenanceRepository,
    odometerRepository: OdometerRepository,
    onAddMaintenance: () -> Unit,
    onEditMaintenance: (Long) -> Unit,
    onPlanFutureMaintenance: () -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val lifecycleOwner = LocalLifecycleOwner.current
    var vehiclePrimary: VehicleListItem? by remember { mutableStateOf(null) }
    var odometerReadings: List<OdometerReading> by remember { mutableStateOf(emptyList()) }
    var maintenances: List<MaintenanceSummary> by remember { mutableStateOf(emptyList()) }
    var refreshTick: Int by remember { mutableIntStateOf(0) }
    var searchDraft: String by remember(vehicleId) { mutableStateOf("") }
    var appliedSearch: String by remember(vehicleId) { mutableStateOf("") }
    var showAddOdometerDialog: Boolean by remember { mutableStateOf(false) }
    var addOdometerDialogSession: Int by remember { mutableIntStateOf(0) }
    var editReading: OdometerReading? by remember { mutableStateOf(null) }
    var deleteReading: OdometerReading? by remember { mutableStateOf(null) }
    var pendingDeleteMaintenance: MaintenanceSummary? by remember { mutableStateOf(null) }
    var deleteLinkedOdometerReading: Boolean by remember { mutableStateOf(false) }
    var pendingDeleteHasLinkedOdometer: Boolean by remember { mutableStateOf(false) }
    val distanceUnit: DistanceUnit = LocalDistanceUnit.current
    val preferSpanish: Boolean = rememberPreferSpanishLocale()

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

    LaunchedEffect(vehicleId, refreshTick, appliedSearch, preferSpanish) {
        vehiclePrimary = vehicleRepository.getVehicleListItem(vehicleId = vehicleId)
        odometerReadings = odometerRepository.getReadingsForVehicle(vehicleId = vehicleId)
        maintenances = maintenanceRepository.getMaintenancesForVehicle(
            vehicleId = vehicleId,
            searchQuery = appliedSearch,
            preferSpanishServiceNames = preferSpanish,
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = ListScreenHorizontalPadding,
                vertical = ListScreenVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(ListScreenSectionSpacing),
    ) {
        item {
            SectionListHeader(
                title = vehiclePrimary?.name ?: stringResource(id = R.string.vehicle_maintenance_vehicle_fallback),
                subtitleBelowTitle = vehicleOdometerSubtitle(
                    lastOdometerKm = vehiclePrimary?.lastOdometerKm,
                ),
                searchQuery = searchDraft,
                onSearchQueryChange = { value: String -> searchDraft = value },
                onSearchSubmit = { appliedSearch = searchDraft.trim() },
                searchPlaceholder = stringResource(id = R.string.vehicle_maintenance_search_visits),
                primaryActionLabel = stringResource(id = R.string.vehicle_maintenance_add),
                onPrimaryAction = onAddMaintenance,
            )
        }
        item {
            VehicleOdometerRegisterSection(
                readings = odometerReadings,
                onAddClick = {
                    addOdometerDialogSession += 1
                    showAddOdometerDialog = true
                },
                onEditReading = { reading: OdometerReading -> editReading = reading },
                onDeleteReading = { reading: OdometerReading -> deleteReading = reading },
            )
        }
        item {
            FilledTonalButton(
                onClick = onPlanFutureMaintenance,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.vehicle_maintenance_plan_future))
            }
        }
        item {
            Text(
                text = stringResource(id = R.string.vehicle_maintenance_section_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(
            items = maintenances,
            key = { m: MaintenanceSummary -> m.id },
        ) { entry: MaintenanceSummary ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
                Text(
                    text = stringResource(
                        id = R.string.vehicle_maintenance_visit_date,
                        entry.performedDate,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val km: Int? = entry.odometerKm
                if (km != null) {
                    Text(
                        text = OdometerUnitConverter.formatStoredKm(km = km, unit = distanceUnit),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (entry.serviceNamesSummary.isNotBlank()) {
                    val servicesSummaryDisplay: String = remember(entry.serviceNamesSummary) {
                        buildServicesSummaryDisplay(rawServiceNamesSummary = entry.serviceNamesSummary)
                    }
                    val hiddenServiceCount: Int = remember(entry.serviceNamesSummary) {
                        countHiddenServices(rawServiceNamesSummary = entry.serviceNamesSummary)
                    }
                    val hiddenServicesSuffix: String = if (hiddenServiceCount > 0) {
                        stringResource(
                            id = R.string.vehicle_maintenance_visit_services_more_count,
                            hiddenServiceCount,
                        )
                    } else {
                        ""
                    }
                    val servicesLine: String = if (hiddenServicesSuffix.isBlank()) {
                        servicesSummaryDisplay
                    } else {
                        "$servicesSummaryDisplay $hiddenServicesSuffix"
                    }
                    Text(
                        text = stringResource(
                            id = R.string.vehicle_maintenance_visit_services,
                            servicesLine,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.vehicle_maintenance_visit_services_none),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onEditMaintenance(entry.id) },
                    ) {
                        Text(text = stringResource(id = R.string.vehicle_maintenance_edit_visit))
                    }
                    TextButton(
                        onClick = {
                            pendingDeleteMaintenance = entry
                            deleteLinkedOdometerReading = false
                            pendingDeleteHasLinkedOdometer =
                                maintenanceRepository.hasOdometerReadingLinked(maintenanceId = entry.id)
                        },
                    ) {
                        Text(text = stringResource(id = R.string.vehicle_maintenance_delete_visit))
                    }
                }
            }
        }
        if (maintenances.isEmpty()) {
            item {
                if (appliedSearch.isBlank()) {
                    SectionEmptyState(
                        message = stringResource(id = R.string.vehicle_maintenance_empty_none),
                        hint = stringResource(id = R.string.vehicle_maintenance_empty_none_hint),
                    )
                } else {
                    SectionEmptyState(
                        message = stringResource(id = R.string.vehicle_maintenance_empty_no_match),
                        hint = stringResource(id = R.string.try_different_search),
                    )
                }
            }
        }
    }

    if (showAddOdometerDialog) {
        key(addOdometerDialogSession) {
            OdometerReadingFormDialog(
                title = stringResource(id = R.string.odometer_dialog_add_title),
                initialKm = "",
                initialDate = todayIsoDate(),
                onDismiss = { showAddOdometerDialog = false },
                onConfirm = { km: Int, date: String ->
                    odometerRepository.insertReading(
                        vehicleId = vehicleId,
                        odometerKm = km,
                        recordedAt = date,
                        maintenanceId = null,
                    )
                    showAddOdometerDialog = false
                    refreshTick += 1
                },
            )
        }
    }

    val readingEdit: OdometerReading? = editReading
    if (readingEdit != null) {
        key(readingEdit.id) {
            OdometerReadingFormDialog(
                title = stringResource(id = R.string.odometer_dialog_edit_title),
                initialKm = OdometerUnitConverter.storedKmToDisplayIntString(
                    km = readingEdit.odometerKm,
                    unit = distanceUnit,
                ),
                initialDate = readingEdit.recordedAt,
                onDismiss = { editReading = null },
                onConfirm = { km: Int, date: String ->
                    odometerRepository.updateReading(
                        readingId = readingEdit.id,
                        odometerKm = km,
                        recordedAt = date,
                    )
                    editReading = null
                    refreshTick += 1
                },
            )
        }
    }

    val readingDelete: OdometerReading? = deleteReading
    if (readingDelete != null) {
        DeleteOdometerReadingDialog(
            reading = readingDelete,
            onDismiss = { deleteReading = null },
            onConfirmDelete = {
                odometerRepository.deleteReading(readingId = readingDelete.id)
                deleteReading = null
                refreshTick += 1
            },
        )
    }

    val maintenanceToDelete: MaintenanceSummary? = pendingDeleteMaintenance
    if (maintenanceToDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteMaintenance = null },
            title = { Text(text = stringResource(id = R.string.vehicle_maintenance_delete_title)) },
            text = {
                ColumnWithDeleteMaintenanceBody(
                    entry = maintenanceToDelete,
                    hasLinkedOdometer = pendingDeleteHasLinkedOdometer,
                    deleteLinkedOdometer = deleteLinkedOdometerReading,
                    onDeleteLinkedChange = { checked: Boolean -> deleteLinkedOdometerReading = checked },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok: Boolean = maintenanceRepository.deleteMaintenance(
                            maintenanceId = maintenanceToDelete.id,
                            deleteLinkedOdometerReadings = deleteLinkedOdometerReading,
                        )
                        pendingDeleteMaintenance = null
                        if (ok) {
                            refreshTick += 1
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.vehicle_maintenance_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteMaintenance = null }) {
                    Text(text = stringResource(id = R.string.vehicle_maintenance_delete_cancel))
                }
            },
        )
    }
}

@Composable
private fun ColumnWithDeleteMaintenanceBody(
    entry: MaintenanceSummary,
    hasLinkedOdometer: Boolean,
    deleteLinkedOdometer: Boolean,
    onDeleteLinkedChange: (Boolean) -> Unit,
): Unit {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(
                id = R.string.vehicle_maintenance_delete_body,
                entry.title,
                entry.performedDate,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (hasLinkedOdometer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = deleteLinkedOdometer,
                    onCheckedChange = onDeleteLinkedChange,
                )
                Text(
                    text = stringResource(id = R.string.vehicle_maintenance_delete_odometer_checkbox),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                )
            }
            Text(
                text = stringResource(id = R.string.vehicle_maintenance_delete_odometer_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun buildServicesSummaryDisplay(rawServiceNamesSummary: String): String {
    val names: List<String> = splitServiceNames(rawServiceNamesSummary = rawServiceNamesSummary)
    return names.firstOrNull().orEmpty()
}

private fun countHiddenServices(rawServiceNamesSummary: String): Int {
    val names: List<String> = splitServiceNames(rawServiceNamesSummary = rawServiceNamesSummary)
    return (names.size - 1).coerceAtLeast(minimumValue = 0)
}

private fun splitServiceNames(rawServiceNamesSummary: String): List<String> {
    return rawServiceNamesSummary
        .split(',')
        .map { name: String -> name.trim() }
        .filter { name: String -> name.isNotBlank() }
}
