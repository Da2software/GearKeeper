package com.example.gearkeeper.ui.screens.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import com.example.gearkeeper.data.local.BrandOption
import com.example.gearkeeper.data.local.ModelOption
import com.example.gearkeeper.data.local.OdometerReminderRepository
import com.example.gearkeeper.data.local.OdometerReminderVehicleStatus
import com.example.gearkeeper.data.local.VehicleCatalogRepository
import com.example.gearkeeper.data.local.VehicleListItem
import com.example.gearkeeper.data.local.VehicleRepository
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionEmptyState
import com.example.gearkeeper.ui.patterns.SectionListHeader
@Composable
fun VehiclesScreen(
    onOpenVehicleMaintenances: (vehicleId: Long) -> Unit,
    onLogMaintenanceForVehicle: (vehicleId: Long) -> Unit,
    onPlanFutureMaintenanceForVehicle: (vehicleId: Long) -> Unit,
    onAddMaintenancePickVehicle: () -> Unit,
    onOpenOdometerReminders: () -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    val catalogRepository: VehicleCatalogRepository = remember(context) {
        VehicleCatalogRepository(context = context)
    }
    val vehicleRepository: VehicleRepository = remember(context) {
        VehicleRepository(context = context)
    }
    val odometerReminderRepository: OdometerReminderRepository = remember(context) {
        OdometerReminderRepository(context = context)
    }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var editingVehicleId: Long? by remember { mutableStateOf(null) }
    var formState by remember { mutableStateOf(VehicleFormState()) }
    var formErrors by remember { mutableStateOf(VehicleFormErrors()) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var brandOptions by remember { mutableStateOf(emptyList<BrandOption>()) }
    var modelOptions by remember { mutableStateOf(emptyList<ModelOption>()) }
    var vehicles by remember { mutableStateOf(emptyList<VehicleListItem>()) }
    var vehiclePendingDelete by remember { mutableStateOf<VehicleListItem?>(null) }
    var pendingReminderCount: Int by remember { mutableStateOf(0) }
    var skippedReminderCount: Int by remember { mutableStateOf(0) }
    var upToDateReminderCount: Int by remember { mutableStateOf(0) }
    var searchDraft: String by remember { mutableStateOf("") }
    var appliedSearch: String by remember { mutableStateOf("") }
    val distanceUnit = LocalDistanceUnit.current

    fun reloadVehicles(): Unit {
        vehicles = vehicleRepository.getAllVehicles(searchQuery = appliedSearch)
        val reminderStatuses = odometerReminderRepository.getVehicleReminderStatuses()
        pendingReminderCount =
            reminderStatuses.count { status -> status.status == OdometerReminderVehicleStatus.Pending }
        skippedReminderCount =
            reminderStatuses.count { status -> status.status == OdometerReminderVehicleStatus.Skipped }
        upToDateReminderCount =
            reminderStatuses.count { status -> status.status == OdometerReminderVehicleStatus.UpToDate }
    }

    LaunchedEffect(Unit) {
        reloadVehicles()
    }

    LaunchedEffect(formState.vehicleType) {
        val vehicleType: String = when (formState.vehicleType) {
            VehicleTypeUi.Car -> "car"
            VehicleTypeUi.Motorcycle -> "motorcycle"
        }
        brandOptions = catalogRepository.getBrands(vehicleType = vehicleType)
        modelOptions = emptyList()
    }

    LaunchedEffect(formState.selectedBrandId) {
        val selectedBrandId: Int? = formState.selectedBrandId
        modelOptions = if (selectedBrandId == null) {
            emptyList()
        } else {
            catalogRepository.getModels(brandId = selectedBrandId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ListScreenHorizontalPadding,
                    vertical = ListScreenVerticalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(ListScreenSectionSpacing),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionListHeader(
                        title = null,
                        searchQuery = searchDraft,
                        onSearchQueryChange = { value: String -> searchDraft = value },
                        onSearchSubmit = {
                            appliedSearch = searchDraft.trim()
                            reloadVehicles()
                        },
                        searchPlaceholder = stringResource(id = R.string.vehicles_search_placeholder),
                        primaryActionLabel = stringResource(id = R.string.vehicles_add_vehicle),
                        onPrimaryAction = {
                            editingVehicleId = null
                            formState = VehicleFormState()
                            formErrors = VehicleFormErrors()
                            submitError = null
                            showAddVehicleDialog = true
                        },
                    )
                    FilledTonalButton(
                        onClick = onAddMaintenancePickVehicle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(text = stringResource(id = R.string.vehicles_add_maintenance))
                    }
                    FilledTonalButton(
                        onClick = onOpenOdometerReminders,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(text = stringResource(id = R.string.vehicles_odometer_reminders))
                    }
                    Text(
                        text = stringResource(
                            id = R.string.vehicles_odometer_reminders_summary,
                            pendingReminderCount,
                            skippedReminderCount,
                            upToDateReminderCount,
                        ),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            items(
                items = vehicles,
                key = { item: VehicleListItem -> item.id },
            ) { item: VehicleListItem ->
                VehicleListRow(
                    item = item,
                    onRowClick = {
                        onOpenVehicleMaintenances(item.id)
                    },
                    onLogMaintenanceClick = {
                        onLogMaintenanceForVehicle(item.id)
                    },
                    onPlanFutureMaintenanceClick = {
                        onPlanFutureMaintenanceForVehicle(item.id)
                    },
                    onEditClick = {
                        val load = vehicleRepository.getVehicleEditLoad(vehicleId = item.id)
                        if (load != null) {
                            editingVehicleId = item.id
                            formState = VehicleFormState(
                                vehicleType = if (load.vehicleType == "motorcycle") {
                                    VehicleTypeUi.Motorcycle
                                } else {
                                    VehicleTypeUi.Car
                                },
                                name = load.name,
                                selectedBrandId = load.brandId,
                                selectedBrandName = load.brandName,
                                selectedModelId = load.modelId,
                                selectedModelName = load.modelName,
                                year = load.year?.toString().orEmpty(),
                                licensePlate = load.licensePlate.orEmpty(),
                                binNumber = load.binNumber.orEmpty(),
                                vin = load.vin.orEmpty(),
                                doors = load.doors?.toString().orEmpty(),
                                currentOdometerKm = load.currentOdometerKm?.let { km: Int ->
                                    OdometerUnitConverter.storedKmToDisplayIntString(
                                        km = km,
                                        unit = distanceUnit,
                                    )
                                }.orEmpty(),
                                odometerReminderEnabled = load.odometerReminderEnabled,
                                odometerReminderIntervalValue = load.odometerReminderIntervalValue.toString(),
                                odometerReminderIntervalUnit = if (load.odometerReminderIntervalUnit == "MONTHS") {
                                    OdometerReminderIntervalUnitUi.Months
                                } else {
                                    OdometerReminderIntervalUnitUi.Days
                                },
                                fuelType = load.fuelType.orEmpty(),
                                transmissionType = load.transmissionType.orEmpty(),
                                transmissionSubtype = load.transmissionSubtype.orEmpty(),
                                color = load.color.orEmpty(),
                                notes = load.notes.orEmpty(),
                            )
                            formErrors = VehicleFormErrors()
                            submitError = null
                            showAddVehicleDialog = true
                        }
                    },
                    onRemoveClick = { vehiclePendingDelete = item },
                )
            }
            if (vehicles.isEmpty()) {
                item {
                    if (appliedSearch.isBlank()) {
                        SectionEmptyState(
                            message = stringResource(id = R.string.vehicles_empty_none),
                            hint = stringResource(id = R.string.vehicles_empty_none_hint),
                        )
                    } else {
                        SectionEmptyState(
                            message = stringResource(id = R.string.vehicles_empty_no_match),
                            hint = stringResource(id = R.string.try_different_search),
                        )
                    }
                }
            }
        }

        val pendingDelete: VehicleListItem? = vehiclePendingDelete
        if (pendingDelete != null) {
            DeleteVehicleConfirmationDialog(
                vehicleName = pendingDelete.name,
                onDismiss = { vehiclePendingDelete = null },
                onConfirmDelete = {
                    val deleted: Boolean =
                        vehicleRepository.deleteVehicleById(vehicleId = pendingDelete.id)
                    vehiclePendingDelete = null
                    if (deleted) {
                        reloadVehicles()
                    }
                },
            )
        }

        if (showAddVehicleDialog) {
            AddVehicleFormDialog(
                dialogTitle = if (editingVehicleId != null) {
                    stringResource(id = R.string.vehicle_form_dialog_title_edit)
                } else {
                    stringResource(id = R.string.vehicle_form_dialog_title)
                },
                state = formState,
                errors = formErrors,
                brands = brandOptions,
                models = modelOptions,
                submitError = submitError,
                onStateChange = { state: VehicleFormState ->
                    formState = state
                    formErrors = validateVehicleForm(context = context, state = state).errors
                    submitError = null
                },
                onDismiss = {
                    submitError = null
                    editingVehicleId = null
                    showAddVehicleDialog = false
                },
                onSubmit = {
                    submitError = null
                    val result: VehicleFormValidationResult =
                        validateVehicleForm(context = context, state = formState)
                    formErrors = result.errors
                    if (result.isValid) {
                        val input = formState.toNewVehicleInput(distanceUnit = distanceUnit)
                        if (input == null) {
                            submitError = context.getString(R.string.vehicles_error_brand_model_required)
                        } else {
                            val saveMessage: String? = if (editingVehicleId != null) {
                                vehicleRepository.updateVehicle(vehicleId = editingVehicleId!!, input = input)
                            } else {
                                vehicleRepository.insertVehicle(input)
                            }
                            if (saveMessage == null) {
                                reloadVehicles()
                                showAddVehicleDialog = false
                                editingVehicleId = null
                                formState = VehicleFormState()
                                formErrors = VehicleFormErrors()
                            } else {
                                submitError = saveMessage
                            }
                        }
                    }
                },
            )
        }
    }
}
