package com.example.gearkeeper.ui.screens.services

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.gearkeeper.R

@Composable
fun DeleteServiceTypeConfirmationDialog(
    serviceName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
): Unit {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.service_editor_delete_confirm_title))
        },
        text = {
            Text(
                text = stringResource(id = R.string.service_editor_delete_confirm_message, serviceName),
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
