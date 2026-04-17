package com.example.gearkeeper.ui.screens.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.ServiceTypeCatalogItem
import com.example.gearkeeper.data.local.ServiceTypeDeleteResult
import com.example.gearkeeper.data.local.ServiceTypeRepository
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionListHeader

@Composable
fun ServiceTypeEditorScreen(
    serviceTypeRepository: ServiceTypeRepository,
    serviceTypeId: Long?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    val isEditMode: Boolean = serviceTypeId != null
    var loaded: ServiceTypeCatalogItem? by remember(serviceTypeId) { mutableStateOf(null) }
    var loadFailed: Boolean by remember { mutableStateOf(false) }
    var name: String by remember { mutableStateOf("") }
    var error: String? by remember { mutableStateOf(null) }
    var pendingDelete: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(serviceTypeId) {
        if (serviceTypeId == null) {
            loaded = null
            name = ""
            loadFailed = false
            return@LaunchedEffect
        }
        val item: ServiceTypeCatalogItem? = serviceTypeRepository.getCatalogItem(id = serviceTypeId)
        if (item == null || item.isSeeded) {
            loadFailed = true
            loaded = null
        } else {
            loaded = item
            name = item.name
            loadFailed = false
        }
    }

    if (loadFailed) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    horizontal = ListScreenHorizontalPadding,
                    vertical = ListScreenVerticalPadding,
                ),
        ) {
            Text(
                text = stringResource(id = R.string.service_editor_cannot_edit),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onFinished) {
                Text(text = stringResource(id = R.string.service_editor_go_back))
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = ListScreenHorizontalPadding,
                vertical = ListScreenVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(ListScreenSectionSpacing),
    ) {
        SectionListHeader(
            title = null,
            supportingText = if (isEditMode) {
                stringResource(id = R.string.service_editor_supporting_edit)
            } else {
                stringResource(id = R.string.service_editor_supporting_new)
            },
        )
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                error = null
            },
            label = { Text(text = stringResource(id = R.string.service_editor_name_label)) },
            singleLine = true,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Button(
            onClick = {
                if (isEditMode && serviceTypeId != null) {
                    val ok: Boolean = serviceTypeRepository.updateCustomServiceTypeName(
                        id = serviceTypeId,
                        newName = name,
                    )
                    if (!ok) {
                        error = context.getString(R.string.service_editor_error_save_duplicate)
                    } else {
                        onFinished()
                    }
                } else {
                    val newId: Long? = serviceTypeRepository.insertCustomName(name = name)
                    if (newId == null) {
                        error = context.getString(R.string.service_editor_error_save_generic)
                    } else {
                        onFinished()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (isEditMode) {
                    stringResource(id = R.string.service_editor_save_changes)
                } else {
                    stringResource(id = R.string.service_editor_save_new)
                },
            )
        }
        if (isEditMode && loaded != null && !loaded!!.isSeeded) {
            OutlinedButton(
                onClick = {
                    pendingDelete = true
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.service_editor_delete))
            }
        }
    }

    if (pendingDelete && loaded != null) {
        DeleteServiceTypeConfirmationDialog(
            serviceName = loaded!!.name,
            onDismiss = { pendingDelete = false },
            onConfirmDelete = {
                pendingDelete = false
                val result: ServiceTypeDeleteResult =
                    serviceTypeRepository.deleteCustomServiceType(id = loaded!!.id)
                when (result) {
                    ServiceTypeDeleteResult.Success -> onFinished()
                    ServiceTypeDeleteResult.NotFoundOrBuiltIn ->
                        error = context.getString(R.string.service_editor_error_delete_blocked)
                    ServiceTypeDeleteResult.InUseByMaintenance ->
                        error = context.getString(R.string.service_editor_error_delete_in_use)
                    ServiceTypeDeleteResult.UnknownFailure ->
                        error = context.getString(R.string.service_editor_error_delete_failed)
                }
            },
        )
    }
}
