package com.example.gearkeeper.ui.screens.vehicles

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.gearkeeper.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DeleteVehicleConfirmationDialog(
    vehicleName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
): Unit {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.vehicle_delete_title))
        },
        text = {
            Text(
                text = stringResource(id = R.string.vehicle_delete_message, vehicleName),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
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
