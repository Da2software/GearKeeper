package com.example.gearkeeper.ui.screens.vehicles

import com.example.gearkeeper.data.local.NewVehicleInput
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.domain.preferences.DistanceUnit

fun VehicleFormState.toNewVehicleInput(distanceUnit: DistanceUnit): NewVehicleInput? {
    val brandId: Int = selectedBrandId ?: return null
    val modelId: Int = selectedModelId ?: return null
    val vehicleTypeDb: String = when (vehicleType) {
        VehicleTypeUi.Car -> "car"
        VehicleTypeUi.Motorcycle -> "motorcycle"
    }
    val yearInt: Int? = year.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
    val doorsInt: Int? = when (vehicleType) {
        VehicleTypeUi.Motorcycle -> null
        VehicleTypeUi.Car -> doors.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
    }
    val odometerTrimmed: String = currentOdometerKm.trim()
    val odometerRaw: Int = odometerTrimmed.toIntOrNull() ?: return null
    if (odometerRaw < 0) {
        return null
    }
    val odometerKmInt: Int = OdometerUnitConverter.userInputToStoredKm(raw = odometerRaw, unit = distanceUnit)
    val odometerLong: Long = odometerKmInt.toLong()
    val reminderIntervalValue: Int = if (odometerReminderEnabled) {
        val parsed: Int = odometerReminderIntervalValue.trim().toIntOrNull() ?: return null
        if (parsed <= 0) {
            return null
        }
        parsed
    } else {
        7
    }
    val reminderUnit: String = when (odometerReminderIntervalUnit) {
        OdometerReminderIntervalUnitUi.Days -> "DAYS"
        OdometerReminderIntervalUnitUi.Months -> "MONTHS"
    }

    return NewVehicleInput(
        vehicleType = vehicleTypeDb,
        brandId = brandId,
        modelId = modelId,
        name = name,
        year = yearInt,
        licensePlate = licensePlate.trim().takeIf { it.isNotEmpty() },
        binNumber = binNumber.trim().takeIf { it.isNotEmpty() },
        vin = vin.trim().takeIf { it.isNotEmpty() },
        doors = doorsInt,
        currentOdometerKm = odometerLong,
        odometerReminderEnabled = odometerReminderEnabled,
        odometerReminderIntervalUnit = reminderUnit,
        odometerReminderIntervalValue = reminderIntervalValue,
        fuelType = fuelType.trim().takeIf { it.isNotEmpty() },
        transmissionType = transmissionType.trim().takeIf { it.isNotEmpty() },
        transmissionSubtype = transmissionSubtype.trim().takeIf { it.isNotEmpty() },
        color = color.trim().takeIf { it.isNotEmpty() },
        notes = notes.trim().takeIf { it.isNotEmpty() },
    )
}
