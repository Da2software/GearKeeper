package com.example.gearkeeper.data.local

data class MaintenanceEditLoad(
    val vehicleId: Long,
    val title: String,
    val performedDate: String,
    val odometerKm: Int?,
    val notes: String?,
    val serviceTypeIds: List<Long>,
    val replacedParts: List<ReplacedPartInput>,
)
