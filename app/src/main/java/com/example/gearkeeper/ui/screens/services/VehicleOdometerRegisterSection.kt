package com.example.gearkeeper.ui.screens.services

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.OdometerReading
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.domain.preferences.DistanceUnit
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import com.example.gearkeeper.ui.util.showIsoDatePickerDialog

@Composable
fun VehicleOdometerRegisterSection(
    readings: List<OdometerReading>,
    onAddClick: () -> Unit,
    onEditReading: (OdometerReading) -> Unit,
    onDeleteReading: (OdometerReading) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val distanceUnit: DistanceUnit = LocalDistanceUnit.current
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.odometer_section_title),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = onAddClick) {
                Text(text = stringResource(id = R.string.odometer_section_add_reading))
            }
            if (readings.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.odometer_section_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(state = rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    readings.forEach { reading: OdometerReading ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = OdometerUnitConverter.formatStoredKm(
                                            km = reading.odometerKm,
                                            unit = distanceUnit,
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = reading.recordedAt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { onEditReading(reading) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(id = R.string.cd_edit_odometer_reading),
                                    )
                                }
                                IconButton(onClick = { onDeleteReading(reading) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(id = R.string.cd_delete_odometer_reading),
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

@Composable
fun OdometerReadingFormDialog(
    title: String,
    initialKm: String,
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (odometerKm: Int, recordedAt: String) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    var kmText: String by remember { mutableStateOf(initialKm) }
    var dateText: String by remember { mutableStateOf(initialDate) }
    var error: String? by remember { mutableStateOf(null) }
    val dateInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val distanceUnit: DistanceUnit = LocalDistanceUnit.current
    val odometerLabel: String = when (distanceUnit) {
        DistanceUnit.METRIC_KM -> stringResource(id = R.string.odometer_label_km)
        DistanceUnit.IMPERIAL_MILES -> stringResource(id = R.string.odometer_label_mi)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.maintenance_date_label)) },
                    singleLine = true,
                    interactionSource = dateInteractionSource,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                showIsoDatePickerDialog(
                                    context = context,
                                    initialIsoDate = dateText,
                                ) { picked: String ->
                                    dateText = picked
                                    error = null
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
                            interactionSource = dateInteractionSource,
                            indication = null,
                        ) {
                            showIsoDatePickerDialog(
                                context = context,
                                initialIsoDate = dateText,
                            ) { picked: String ->
                                dateText = picked
                                error = null
                            }
                        },
                )
                OutlinedTextField(
                    value = kmText,
                    onValueChange = { value: String ->
                        kmText = value.filter { character: Char -> character.isDigit() }
                        error = null
                    },
                    label = { Text(text = odometerLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dateText.isBlank()) {
                        error = context.getString(R.string.odometer_dialog_error_date_required)
                        return@TextButton
                    }
                    if (kmText.isBlank()) {
                        error = context.getString(R.string.odometer_dialog_error_odometer_required)
                        return@TextButton
                    }
                    val raw: Int = kmText.toIntOrNull() ?: run {
                        error = context.getString(R.string.odometer_dialog_error_odometer_number)
                        return@TextButton
                    }
                    if (raw < 0) {
                        error = context.getString(R.string.odometer_dialog_error_odometer_negative)
                        return@TextButton
                    }
                    val kmStored: Int = OdometerUnitConverter.userInputToStoredKm(
                        raw = raw,
                        unit = distanceUnit,
                    )
                    onConfirm(kmStored, dateText.trim())
                },
            ) {
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
fun DeleteOdometerReadingDialog(
    reading: OdometerReading,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val distanceUnit: DistanceUnit = LocalDistanceUnit.current
    val readingLabel: String = OdometerUnitConverter.formatStoredKm(
        km = reading.odometerKm,
        unit = distanceUnit,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = stringResource(id = R.string.odometer_dialog_delete_title)) },
        text = {
            Text(
                text = stringResource(
                    id = R.string.odometer_dialog_delete_message,
                    readingLabel,
                    reading.recordedAt,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
            ) {
                Text(text = stringResource(id = R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
    )
}
