package com.example.gearkeeper.ui.screens.vehicles

enum class VehicleTypeUi {
    Car,
    Motorcycle,
}

enum class OdometerReminderIntervalUnitUi {
    Days,
    Months,
}

data class VehicleFormState(
    val vehicleType: VehicleTypeUi = VehicleTypeUi.Car,
    val name: String = "",
    val selectedBrandId: Int? = null,
    val selectedBrandName: String = "",
    val selectedModelId: Int? = null,
    val selectedModelName: String = "",
    val year: String = "",
    val licensePlate: String = "",
    val binNumber: String = "",
    val vin: String = "",
    val doors: String = "",
    val currentOdometerKm: String = "",
    val odometerReminderEnabled: Boolean = false,
    val odometerReminderIntervalValue: String = "7",
    val odometerReminderIntervalUnit: OdometerReminderIntervalUnitUi = OdometerReminderIntervalUnitUi.Days,
    val fuelType: String = "Gasoline",
    val transmissionType: String = "Automatic",
    val transmissionSubtype: String = "",
    val color: String = "",
    val notes: String = "",
)

data class VehicleFormErrors(
    val name: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val year: String? = null,
    val vin: String? = null,
    val doors: String? = null,
    val currentOdometerKm: String? = null,
    val odometerReminderIntervalValue: String? = null,
)

data class VehicleFormValidationResult(
    val isValid: Boolean,
    val errors: VehicleFormErrors,
)

