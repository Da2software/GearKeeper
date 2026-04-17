package com.example.gearkeeper.data.local

data class NewVehicleInput(
    val vehicleType: String,
    val brandId: Int,
    val modelId: Int,
    val name: String,
    val year: Int?,
    val licensePlate: String?,
    val binNumber: String?,
    val vin: String?,
    val doors: Int?,
    val currentOdometerKm: Long?,
    val odometerReminderEnabled: Boolean,
    val odometerReminderIntervalUnit: String,
    val odometerReminderIntervalValue: Int,
    val fuelType: String?,
    val transmissionType: String?,
    val transmissionSubtype: String?,
    val color: String?,
    val notes: String?,
)
