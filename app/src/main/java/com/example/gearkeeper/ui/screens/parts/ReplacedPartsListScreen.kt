package com.example.gearkeeper.ui.screens.parts

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.MaintenancePickerRow
import com.example.gearkeeper.data.local.ReplacedPartInput
import com.example.gearkeeper.data.local.ReplacedPartListFilter
import com.example.gearkeeper.data.local.ReplacedPartListRow
import com.example.gearkeeper.data.local.ReplacedPartRepository
import com.example.gearkeeper.data.local.SqlSearch
import com.example.gearkeeper.data.local.VehicleListItem
import com.example.gearkeeper.data.local.VehicleRepository
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionListHeader

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReplacedPartsListScreen(
    replacedPartRepository: ReplacedPartRepository,
    vehicleRepository: VehicleRepository,
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    var searchQuery: String by remember { mutableStateOf("") }
    /** Applied when the user submits the header search (matches title, part #, brand, store, notes, maintenance title). */
    var appliedFullTextNeedle: String? by remember { mutableStateOf(null) }
    var filtersExpanded: Boolean by remember { mutableStateOf(false) }
    var draftVehicleId: Long? by remember { mutableStateOf(null) }
    var draftDateFrom: String by remember { mutableStateOf("") }
    var draftDateTo: String by remember { mutableStateOf("") }
    var draftPartSerial: String by remember { mutableStateOf("") }
    var draftBrand: String by remember { mutableStateOf("") }
    var draftStore: String by remember { mutableStateOf("") }
    var draftNotes: String by remember { mutableStateOf("") }
    var appliedVehicleId: Long? by remember { mutableStateOf(null) }
    var appliedDateFrom: String? by remember { mutableStateOf(null) }
    var appliedDateTo: String? by remember { mutableStateOf(null) }
    var appliedPartSerialNeedle: String? by remember { mutableStateOf(null) }
    var appliedBrandNeedle: String? by remember { mutableStateOf(null) }
    var appliedStoreNeedle: String? by remember { mutableStateOf(null) }
    var appliedNotesNeedle: String? by remember { mutableStateOf(null) }
    var vehicleOptions: List<VehicleListItem> by remember { mutableStateOf(emptyList()) }
    var vehicleMenuExpanded: Boolean by remember { mutableStateOf(false) }
    var vehicleMenuLabel: String by remember { mutableStateOf("") }
    var rows: List<ReplacedPartListRow> by remember { mutableStateOf(emptyList()) }
    var deleteTarget: ReplacedPartListRow? by remember { mutableStateOf(null) }
    var editTarget: ReplacedPartListRow? by remember { mutableStateOf(null) }
    var addDialogOpen: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vehicleOptions = vehicleRepository.getAllVehicles()
    }

    LaunchedEffect(
        appliedFullTextNeedle,
        appliedVehicleId,
        appliedDateFrom,
        appliedDateTo,
        appliedPartSerialNeedle,
        appliedBrandNeedle,
        appliedStoreNeedle,
        appliedNotesNeedle,
    ) {
        rows = replacedPartRepository.listReplacedParts(
            filter = ReplacedPartListFilter(
                fullTextNeedle = appliedFullTextNeedle,
                vehicleId = appliedVehicleId,
                performedDateFrom = appliedDateFrom,
                performedDateTo = appliedDateTo,
                partOrSerialNeedle = appliedPartSerialNeedle,
                brandNeedle = appliedBrandNeedle,
                storeNeedle = appliedStoreNeedle,
                notesNeedle = appliedNotesNeedle,
            ),
        )
    }

    fun runHeaderTextSearch(): Unit {
        appliedFullTextNeedle = SqlSearch.normalizeNeedle(raw = searchQuery)
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
                SectionListHeader(
                    title = null,
                    supportingText = stringResource(id = R.string.replaced_parts_intro),
                    searchQuery = searchQuery,
                    onSearchQueryChange = { value: String -> searchQuery = value },
                    onSearchSubmit = { runHeaderTextSearch() },
                    searchPlaceholder = stringResource(id = R.string.replaced_parts_search_name_placeholder),
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filtersExpanded = !filtersExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.replaced_parts_filters_toggle),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (filtersExpanded) {
                            Icons.Filled.ExpandLess
                        } else {
                            Icons.Filled.ExpandMore
                        },
                        contentDescription = null,
                    )
                }
            }
            if (filtersExpanded) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (vehicleOptions.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.replaced_parts_no_vehicles),
                                style = MaterialTheme.typography.bodySmall,
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
                                    label = { Text(text = stringResource(id = R.string.replaced_parts_filter_vehicle)) },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleMenuExpanded)
                                    },
                                )
                                DropdownMenu(
                                    expanded = vehicleMenuExpanded,
                                    onDismissRequest = { vehicleMenuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.replaced_parts_filter_vehicle_any)) },
                                        onClick = {
                                            draftVehicleId = null
                                            vehicleMenuLabel = ""
                                            vehicleMenuExpanded = false
                                        },
                                    )
                                    vehicleOptions.forEach { v: VehicleListItem ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = "${v.name} — ${v.brandName} · ${v.modelName}")
                                            },
                                            onClick = {
                                                draftVehicleId = v.id
                                                vehicleMenuLabel = "${v.name} — ${v.brandName} · ${v.modelName}"
                                                vehicleMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = draftDateFrom,
                            onValueChange = { draftDateFrom = it },
                            label = { Text(text = stringResource(id = R.string.replaced_parts_filter_date_from)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = stringResource(id = R.string.replaced_parts_date_hint_yyyy_mm_dd)) },
                        )
                        OutlinedTextField(
                            value = draftDateTo,
                            onValueChange = { draftDateTo = it },
                            label = { Text(text = stringResource(id = R.string.replaced_parts_filter_date_to)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = stringResource(id = R.string.replaced_parts_date_hint_yyyy_mm_dd)) },
                        )
                        OutlinedTextField(
                            value = draftPartSerial,
                            onValueChange = { draftPartSerial = it },
                            label = { Text(text = stringResource(id = R.string.replaced_parts_filter_part_serial)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = draftBrand,
                            onValueChange = { draftBrand = it },
                            label = { Text(text = stringResource(id = R.string.replaced_parts_filter_brand)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = draftStore,
                            onValueChange = { draftStore = it },
                            label = { Text(text = stringResource(id = R.string.replaced_parts_filter_store)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { draftNotes = it },
                            label = { Text(text = stringResource(id = R.string.replaced_parts_filter_notes)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = {
                                appliedVehicleId = draftVehicleId
                                appliedDateFrom = draftDateFrom.trim().takeIf { it.isNotEmpty() }
                                appliedDateTo = draftDateTo.trim().takeIf { it.isNotEmpty() }
                                appliedPartSerialNeedle = SqlSearch.normalizeNeedle(raw = draftPartSerial)
                                appliedBrandNeedle = SqlSearch.normalizeNeedle(raw = draftBrand)
                                appliedStoreNeedle = SqlSearch.normalizeNeedle(raw = draftStore)
                                appliedNotesNeedle = SqlSearch.normalizeNeedle(raw = draftNotes)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(id = R.string.replaced_parts_apply_filters))
                        }
                    }
                }
            }
            if (rows.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.replaced_parts_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(items = rows, key = { row: ReplacedPartListRow -> row.id }) { row: ReplacedPartListRow ->
                    ReplacedPartCard(
                        row = row,
                        onEdit = { editTarget = row },
                        onDelete = { deleteTarget = row },
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { addDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.replaced_parts_cd_add),
            )
        }
    }

    deleteTarget?.let { target: ReplacedPartListRow ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(text = stringResource(id = R.string.replaced_parts_delete_title)) },
            text = { Text(text = stringResource(id = R.string.replaced_parts_delete_body, target.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok: Boolean = replacedPartRepository.deletePart(id = target.id)
                        deleteTarget = null
                        if (ok) {
                            rows = replacedPartRepository.listReplacedParts(
                                filter = ReplacedPartListFilter(
                                    fullTextNeedle = appliedFullTextNeedle,
                                    vehicleId = appliedVehicleId,
                                    performedDateFrom = appliedDateFrom,
                                    performedDateTo = appliedDateTo,
                                    partOrSerialNeedle = appliedPartSerialNeedle,
                                    brandNeedle = appliedBrandNeedle,
                                    storeNeedle = appliedStoreNeedle,
                                    notesNeedle = appliedNotesNeedle,
                                ),
                            )
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.replaced_parts_delete_failed),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.replaced_parts_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(text = stringResource(id = R.string.replaced_parts_dialog_cancel))
                }
            },
        )
    }

    editTarget?.let { target: ReplacedPartListRow ->
        ReplacedPartEditorDialog(
            titleRes = R.string.replaced_parts_edit_title,
            initialTitle = target.title,
            initialPartOrSerial = target.partOrSerial,
            initialBrand = target.brand.orEmpty(),
            initialStore = target.store.orEmpty(),
            initialNotes = target.notes.orEmpty(),
            onDismiss = { editTarget = null },
            onSave = { input: ReplacedPartInput ->
                val ok: Boolean = replacedPartRepository.updatePart(id = target.id, input = input)
                editTarget = null
                if (ok) {
                    rows = replacedPartRepository.listReplacedParts(
                        filter = ReplacedPartListFilter(
                            fullTextNeedle = appliedFullTextNeedle,
                            vehicleId = appliedVehicleId,
                            performedDateFrom = appliedDateFrom,
                            performedDateTo = appliedDateTo,
                            partOrSerialNeedle = appliedPartSerialNeedle,
                            brandNeedle = appliedBrandNeedle,
                            storeNeedle = appliedStoreNeedle,
                            notesNeedle = appliedNotesNeedle,
                        ),
                    )
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.replaced_parts_save_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )
    }

    if (addDialogOpen) {
        AddReplacedPartDialog(
            replacedPartRepository = replacedPartRepository,
            onDismiss = { addDialogOpen = false },
            onSaved = {
                addDialogOpen = false
                rows = replacedPartRepository.listReplacedParts(
                    filter = ReplacedPartListFilter(
                        fullTextNeedle = appliedFullTextNeedle,
                        vehicleId = appliedVehicleId,
                        performedDateFrom = appliedDateFrom,
                        performedDateTo = appliedDateTo,
                        partOrSerialNeedle = appliedPartSerialNeedle,
                        brandNeedle = appliedBrandNeedle,
                        storeNeedle = appliedStoreNeedle,
                        notesNeedle = appliedNotesNeedle,
                    ),
                )
            },
        )
    }
}

@Composable
private fun ReplacedPartCard(
    row: ReplacedPartListRow,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
): Unit {
    var detailsExpanded: Boolean by remember(row.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = row.title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(
                    id = R.string.replaced_parts_field_part,
                    row.partOrSerial.ifBlank { stringResource(id = R.string.odometer_no_reading_em_dash) },
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { detailsExpanded = !detailsExpanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        id = if (detailsExpanded) {
                            R.string.replaced_parts_collapse_details
                        } else {
                            R.string.replaced_parts_expand_details
                        },
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (detailsExpanded) {
                        Icons.Filled.ExpandLess
                    } else {
                        Icons.Filled.ExpandMore
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (detailsExpanded) {
                Text(
                    text = row.vehicleSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        id = R.string.replaced_parts_meta_line,
                        row.maintenanceTitle,
                        row.performedDate,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                row.brand?.takeIf { it.isNotBlank() }?.let { b: String ->
                    Text(text = stringResource(id = R.string.replaced_parts_field_brand, b))
                }
                row.store?.takeIf { it.isNotBlank() }?.let { s: String ->
                    Text(text = stringResource(id = R.string.replaced_parts_field_store, s))
                }
                row.notes?.takeIf { it.isNotBlank() }?.let { n: String ->
                    Text(text = stringResource(id = R.string.replaced_parts_field_notes, n))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.replaced_parts_cd_edit),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.replaced_parts_cd_delete),
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddReplacedPartDialog(
    replacedPartRepository: ReplacedPartRepository,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
): Unit {
    val context = LocalContext.current
    var pickerRows: List<MaintenancePickerRow> by remember { mutableStateOf(emptyList()) }
    var selectedMaintenanceId: Long? by remember { mutableStateOf(null) }
    var pickerExpanded: Boolean by remember { mutableStateOf(false) }
    var pickerLabel: String by remember { mutableStateOf("") }
    var title: String by remember { mutableStateOf("") }
    var partOrSerial: String by remember { mutableStateOf("") }
    var brand: String by remember { mutableStateOf("") }
    var store: String by remember { mutableStateOf("") }
    var notes: String by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        pickerRows = replacedPartRepository.listRecentMaintenances(limit = 80)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.replaced_parts_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pickerRows.isEmpty()) {
                    Text(text = stringResource(id = R.string.replaced_parts_add_no_maintenance))
                } else {
                    ExposedDropdownMenuBox(
                        expanded = pickerExpanded,
                        onExpandedChange = { expanded: Boolean -> pickerExpanded = expanded },
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            value = pickerLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = stringResource(id = R.string.replaced_parts_add_pick_maintenance)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = pickerExpanded)
                            },
                        )
                        DropdownMenu(
                            expanded = pickerExpanded,
                            onDismissRequest = { pickerExpanded = false },
                        ) {
                            pickerRows.forEach { row: MaintenancePickerRow ->
                                DropdownMenuItem(
                                    text = { Text(text = row.label, maxLines = 2) },
                                    onClick = {
                                        selectedMaintenanceId = row.maintenanceId
                                        pickerLabel = row.label
                                        pickerExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = partOrSerial,
                    onValueChange = { partOrSerial = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_part_serial)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_brand)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = store,
                    onValueChange = { store = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_store)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val mid: Long = selectedMaintenanceId ?: run {
                        Toast.makeText(
                            context,
                            context.getString(R.string.replaced_parts_add_error_maintenance),
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@TextButton
                    }
                    if (title.isBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.replaced_parts_add_error_title),
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@TextButton
                    }
                    val ok: Boolean = replacedPartRepository.insertStandalone(
                        maintenanceId = mid,
                        input = ReplacedPartInput(
                            title = title,
                            partOrSerial = partOrSerial,
                            brand = brand.takeIf { it.isNotBlank() },
                            store = store.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                        ),
                    )
                    if (ok) {
                        onSaved()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.replaced_parts_save_failed),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            ) {
                Text(text = stringResource(id = R.string.replaced_parts_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.replaced_parts_dialog_cancel))
            }
        },
    )
}

@Composable
private fun ReplacedPartEditorDialog(
    titleRes: Int,
    initialTitle: String,
    initialPartOrSerial: String,
    initialBrand: String,
    initialStore: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (ReplacedPartInput) -> Unit,
): Unit {
    var title: String by remember { mutableStateOf(initialTitle) }
    var partOrSerial: String by remember { mutableStateOf(initialPartOrSerial) }
    var brand: String by remember { mutableStateOf(initialBrand) }
    var store: String by remember { mutableStateOf(initialStore) }
    var notes: String by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = titleRes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = partOrSerial,
                    onValueChange = { partOrSerial = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_part_serial)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_brand)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = store,
                    onValueChange = { store = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_store)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(text = stringResource(id = R.string.replaced_parts_draft_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ReplacedPartInput(
                            title = title,
                            partOrSerial = partOrSerial,
                            brand = brand.takeIf { it.isNotBlank() },
                            store = store.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                        ),
                    )
                },
            ) {
                Text(text = stringResource(id = R.string.replaced_parts_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.replaced_parts_dialog_cancel))
            }
        },
    )
}
