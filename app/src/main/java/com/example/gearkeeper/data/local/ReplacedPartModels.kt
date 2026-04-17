package com.example.gearkeeper.data.local

data class ReplacedPartInput(
    val title: String,
    val partOrSerial: String,
    val brand: String?,
    val store: String?,
    val notes: String?,
)

data class ReplacedPartListRow(
    val id: Long,
    val maintenanceId: Long,
    val vehicleId: Long,
    val vehicleSummary: String,
    val maintenanceTitle: String,
    val performedDate: String,
    val title: String,
    val partOrSerial: String,
    val brand: String?,
    val store: String?,
    val notes: String?,
    val createdAt: String,
)

data class MaintenancePickerRow(
    val maintenanceId: Long,
    val vehicleId: Long,
    val label: String,
)

data class ReplacedPartListFilter(
    /** Main search: matches any of title, part/serial, brand, store, notes (and maintenance title). */
    val fullTextNeedle: String? = null,
    val vehicleId: Long? = null,
    val performedDateFrom: String? = null,
    val performedDateTo: String? = null,
    val partOrSerialNeedle: String? = null,
    val brandNeedle: String? = null,
    val storeNeedle: String? = null,
    val notesNeedle: String? = null,
)

/** Row from history for picking into a new maintenance (catalog search). */
data class ReplacedPartHistoryPick(
    val id: Long,
    val title: String,
    val partOrSerial: String,
    val brand: String?,
    val store: String?,
    val notes: String?,
)
