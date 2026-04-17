package com.example.gearkeeper.ui.screens.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.OdometerReminderRepository
import com.example.gearkeeper.data.local.OdometerReminderVehicleStatus
import com.example.gearkeeper.data.local.OdometerReminderVehicleStatusRow
import com.example.gearkeeper.reminders.OdometerReminderScheduler
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionEmptyState
import com.example.gearkeeper.ui.patterns.SectionListHeader

@Composable
fun OdometerReminderPanelScreen(
    odometerReminderRepository: OdometerReminderRepository,
    onOpenVehicleMaintenances: (Long) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    var statusRows: List<OdometerReminderVehicleStatusRow> by remember { mutableStateOf(emptyList()) }
    var searchDraft: String by remember { mutableStateOf("") }
    var appliedSearch: String by remember { mutableStateOf("") }

    fun reload(): Unit {
        statusRows = odometerReminderRepository.getVehicleReminderStatuses().filter { row: OdometerReminderVehicleStatusRow ->
            val needle: String = appliedSearch.trim()
            if (needle.isBlank()) {
                true
            } else {
                row.vehicleName.contains(other = needle, ignoreCase = true) ||
                    row.vehicleBrandModelLine.contains(other = needle, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(appliedSearch) {
        reload()
    }

    val pendingRows: List<OdometerReminderVehicleStatusRow> = statusRows.filter { row: OdometerReminderVehicleStatusRow ->
        row.status == OdometerReminderVehicleStatus.Pending
    }
    val skippedRows: List<OdometerReminderVehicleStatusRow> = statusRows.filter { row: OdometerReminderVehicleStatusRow ->
        row.status == OdometerReminderVehicleStatus.Skipped
    }
    val upToDateRows: List<OdometerReminderVehicleStatusRow> = statusRows.filter { row: OdometerReminderVehicleStatusRow ->
        row.status == OdometerReminderVehicleStatus.UpToDate
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
                supportingText = stringResource(
                    id = R.string.odometer_reminder_panel_summary,
                    pendingRows.size,
                ),
                searchQuery = searchDraft,
                onSearchQueryChange = { value: String -> searchDraft = value },
                onSearchSubmit = { appliedSearch = searchDraft.trim() },
                searchPlaceholder = stringResource(id = R.string.odometer_reminder_panel_search_placeholder),
            )
        }
        if (statusRows.isEmpty()) {
            item {
                SectionEmptyState(
                    message = stringResource(id = R.string.odometer_reminder_panel_empty_title),
                    hint = stringResource(id = R.string.odometer_reminder_panel_empty_hint),
                )
            }
        } else {
            if (pendingRows.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.odometer_reminder_section_pending),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            items(
                items = pendingRows,
                key = { row: OdometerReminderVehicleStatusRow -> "pending-${row.vehicleId}" },
            ) { row: OdometerReminderVehicleStatusRow ->
                ReminderStatusCard(
                    row = row,
                    skipLabel = stringResource(id = R.string.odometer_reminder_skip),
                    updateNowLabel = stringResource(id = R.string.odometer_reminder_update_now),
                    latestLabel = stringResource(
                        id = R.string.odometer_reminder_panel_latest_reading_label,
                        row.latestReadingCreatedAt ?: stringResource(id = R.string.odometer_no_reading_em_dash),
                    ),
                    onSkip = {
                        val skipped: Boolean =
                            odometerReminderRepository.skipVehicleReminder(vehicleId = row.vehicleId)
                        if (skipped) {
                            OdometerReminderScheduler.scheduleImmediateCheck(context = context)
                            reload()
                        }
                    },
                    onUpdateNow = { onOpenVehicleMaintenances(row.vehicleId) },
                )
            }
            if (skippedRows.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.odometer_reminder_section_skipped),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            items(
                items = skippedRows,
                key = { row: OdometerReminderVehicleStatusRow -> "skipped-${row.vehicleId}" },
            ) { row: OdometerReminderVehicleStatusRow ->
                ReminderStatusCard(
                    row = row,
                    skipLabel = stringResource(id = R.string.odometer_reminder_resume),
                    updateNowLabel = stringResource(id = R.string.odometer_reminder_update_now),
                    latestLabel = stringResource(
                        id = R.string.odometer_reminder_panel_skip_until_label,
                        row.skipUntil ?: stringResource(id = R.string.odometer_no_reading_em_dash),
                    ),
                    onSkip = {
                        val resumed: Boolean =
                            odometerReminderRepository.clearReminderSkip(vehicleId = row.vehicleId)
                        if (resumed) {
                            OdometerReminderScheduler.scheduleImmediateCheck(context = context)
                            reload()
                        }
                    },
                    onUpdateNow = { onOpenVehicleMaintenances(row.vehicleId) },
                )
            }
            if (upToDateRows.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.odometer_reminder_section_up_to_date),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            items(
                items = upToDateRows,
                key = { row: OdometerReminderVehicleStatusRow -> "uptodate-${row.vehicleId}" },
            ) { row: OdometerReminderVehicleStatusRow ->
                ReminderStatusCard(
                    row = row,
                    skipLabel = stringResource(id = R.string.odometer_reminder_skip),
                    updateNowLabel = stringResource(id = R.string.odometer_reminder_update_now),
                    latestLabel = stringResource(
                        id = R.string.odometer_reminder_panel_next_due_label,
                        row.nextDueAt,
                    ),
                    onSkip = {
                        val skipped: Boolean =
                            odometerReminderRepository.skipVehicleReminder(vehicleId = row.vehicleId)
                        if (skipped) {
                            OdometerReminderScheduler.scheduleImmediateCheck(context = context)
                            reload()
                        }
                    },
                    onUpdateNow = { onOpenVehicleMaintenances(row.vehicleId) },
                )
            }
        }
    }
}

@Composable
private fun ReminderStatusCard(
    row: OdometerReminderVehicleStatusRow,
    skipLabel: String,
    updateNowLabel: String,
    latestLabel: String,
    onSkip: () -> Unit,
    onUpdateNow: () -> Unit,
): Unit {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = row.vehicleName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (row.vehicleBrandModelLine.isNotBlank()) {
                Text(
                    text = row.vehicleBrandModelLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = latestLabel,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onSkip) {
                    Text(text = skipLabel)
                }
                TextButton(onClick = onUpdateNow) {
                    Text(text = updateNowLabel)
                }
            }
        }
    }
}
