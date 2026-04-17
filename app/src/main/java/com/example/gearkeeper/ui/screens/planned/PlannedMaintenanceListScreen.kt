package com.example.gearkeeper.ui.screens.planned

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.PlannedMaintenanceIncomingRow
import com.example.gearkeeper.data.local.PlannedMaintenanceRepository
import com.example.gearkeeper.domain.preferences.DistanceUnit
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionEmptyState
import com.example.gearkeeper.ui.patterns.SectionListHeader
import com.example.gearkeeper.ui.screens.incoming.IncomingServiceStatus
import com.example.gearkeeper.ui.patterns.matchesListSearch
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import com.example.gearkeeper.ui.screens.incoming.IncomingServiceUiModel
import com.example.gearkeeper.ui.screens.incoming.compareIncomingByUrgency
import com.example.gearkeeper.ui.screens.incoming.todayLocalDate
import com.example.gearkeeper.ui.screens.incoming.toIncomingServiceUiModel
import com.example.gearkeeper.ui.util.rememberPreferSpanishLocale

private enum class PlannedStatusFilter {
    All,
    Overdue,
    ReminderWindow,
    Scheduled,
}

@Composable
fun PlannedMaintenanceListScreen(
    plannedMaintenanceRepository: PlannedMaintenanceRepository,
    onAddPlanned: () -> Unit,
    onOpenMaintenance: (vehicleId: Long, presetServiceTypeId: Long) -> Unit,
    onEditPlanned: (plannedMaintenanceId: Long) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val lifecycleOwner = LocalLifecycleOwner.current
    var rowsFromDb: List<PlannedMaintenanceIncomingRow> by remember { mutableStateOf(emptyList()) }
    var refreshNonce: Int by remember { mutableIntStateOf(0) }
    var pendingDelete: IncomingServiceUiModel? by remember { mutableStateOf(null) }
    var searchDraft: String by remember { mutableStateOf("") }
    var appliedSearch: String by remember { mutableStateOf("") }
    var selectedStatusFilter: PlannedStatusFilter by remember { mutableStateOf(PlannedStatusFilter.All) }
    val distanceUnit: DistanceUnit = LocalDistanceUnit.current
    val preferSpanish: Boolean = rememberPreferSpanishLocale()
    val expiresDateFmt: String = stringResource(id = R.string.planned_list_card_expires_date)
    val reminderDateFmt: String = stringResource(id = R.string.planned_list_card_reminder_date)
    val expiresOdoFmt: String = stringResource(id = R.string.planned_list_card_expires_odometer)
    val reminderOdoFmt: String = stringResource(id = R.string.planned_list_card_reminder_odometer)

    LaunchedEffect(lifecycleOwner, preferSpanish, refreshNonce) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            rowsFromDb = plannedMaintenanceRepository.getAllPlannedIncomingRows(
                preferSpanishServiceNames = preferSpanish,
            )
        }
    }

    val uiModels: List<IncomingServiceUiModel> = remember(
        rowsFromDb,
        distanceUnit,
        expiresDateFmt,
        reminderDateFmt,
        expiresOdoFmt,
        reminderOdoFmt,
    ) {
        val todayResolved = todayLocalDate()
        rowsFromDb
            .map { row: PlannedMaintenanceIncomingRow ->
                row.toIncomingServiceUiModel(
                    distanceUnit = distanceUnit,
                    today = todayResolved,
                    expiresDateTemplate = expiresDateFmt,
                    reminderDateTemplate = reminderDateFmt,
                    expiresOdometerTemplate = expiresOdoFmt,
                    reminderOdometerTemplate = reminderOdoFmt,
                )
            }
            .sortedWith(::compareIncomingByUrgency)
    }

    val filtered: List<IncomingServiceUiModel> = remember(appliedSearch, selectedStatusFilter, uiModels) {
        uiModels.filter { item: IncomingServiceUiModel ->
            val matchesStatus: Boolean = when (selectedStatusFilter) {
                PlannedStatusFilter.All -> true
                PlannedStatusFilter.Overdue -> item.status == IncomingServiceStatus.Overdue
                PlannedStatusFilter.ReminderWindow -> item.status == IncomingServiceStatus.ReminderWindow
                PlannedStatusFilter.Scheduled -> item.status == IncomingServiceStatus.Scheduled
            }
            val matchesText: Boolean = matchesListSearch(
                appliedSearch,
                item.planTitle,
                item.vehicleName,
                item.vehicleBrandModelLine,
                item.expiresLine,
                item.reminderLine,
            )
            matchesStatus && matchesText
        }
    }

    pendingDelete?.let { target: IncomingServiceUiModel ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text(text = stringResource(id = R.string.planned_delete_confirm_title))
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.planned_delete_confirm_message,
                        target.planTitle,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (plannedMaintenanceRepository.deletePlannedMaintenance(target.plannedMaintenanceId)) {
                            refreshNonce++
                        }
                        pendingDelete = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
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
                title = null,
                searchQuery = searchDraft,
                onSearchQueryChange = { value: String -> searchDraft = value },
                onSearchSubmit = { appliedSearch = searchDraft.trim() },
                searchPlaceholder = stringResource(id = R.string.planned_list_search_placeholder),
                primaryActionLabel = stringResource(id = R.string.planned_list_add),
                onPrimaryAction = onAddPlanned,
            )
        }
        item {
            val horizontalChipScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(state = horizontalChipScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedStatusFilter == PlannedStatusFilter.All,
                    onClick = { selectedStatusFilter = PlannedStatusFilter.All },
                    label = { Text(text = stringResource(id = R.string.planned_list_status_all)) },
                )
                FilterChip(
                    selected = selectedStatusFilter == PlannedStatusFilter.Overdue,
                    onClick = { selectedStatusFilter = PlannedStatusFilter.Overdue },
                    label = { Text(text = stringResource(id = R.string.incoming_status_overdue)) },
                )
                FilterChip(
                    selected = selectedStatusFilter == PlannedStatusFilter.ReminderWindow,
                    onClick = { selectedStatusFilter = PlannedStatusFilter.ReminderWindow },
                    label = { Text(text = stringResource(id = R.string.incoming_status_reminder_window)) },
                )
                FilterChip(
                    selected = selectedStatusFilter == PlannedStatusFilter.Scheduled,
                    onClick = { selectedStatusFilter = PlannedStatusFilter.Scheduled },
                    label = { Text(text = stringResource(id = R.string.incoming_status_scheduled)) },
                )
            }
        }
        items(
            items = filtered,
            key = { item: IncomingServiceUiModel -> item.plannedMaintenanceId },
        ) { item: IncomingServiceUiModel ->
            PlannedMaintenanceListItem(
                item = item,
                onLogMaintenance = {
                    val preset: Long = item.presetServiceTypeId ?: -1L
                    onOpenMaintenance(item.vehicleId, preset)
                },
                onEditPlanned = { onEditPlanned(item.plannedMaintenanceId) },
                onDeletePlanned = { pendingDelete = item },
            )
        }
        if (uiModels.isEmpty()) {
            item {
                SectionEmptyState(
                    message = stringResource(id = R.string.planned_list_empty_none),
                    hint = stringResource(id = R.string.planned_list_empty_hint),
                )
            }
        } else if (filtered.isEmpty()) {
            item {
                SectionEmptyState(
                    message = stringResource(id = R.string.planned_list_empty_no_match),
                    hint = stringResource(id = R.string.try_different_search),
                )
            }
        }
    }
}
