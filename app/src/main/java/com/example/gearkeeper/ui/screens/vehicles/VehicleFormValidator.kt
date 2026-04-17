package com.example.gearkeeper.ui.screens.vehicles

import android.content.Context
import com.example.gearkeeper.R

fun validateVehicleForm(context: Context, state: VehicleFormState): VehicleFormValidationResult {
    val nameError: String? = if (state.name.trim().isEmpty()) {
        context.getString(R.string.vehicle_validation_name_required)
    } else {
        null
    }
    val brandError: String? = if (state.selectedBrandId == null) {
        context.getString(R.string.vehicle_validation_brand_required)
    } else {
        null
    }
    val modelError: String? = if (state.selectedModelId == null) {
        context.getString(R.string.vehicle_validation_model_required)
    } else {
        null
    }
    val yearError: String? = when {
        state.year.isBlank() -> null
        state.year.toIntOrNull() == null -> context.getString(R.string.vehicle_validation_year_numeric)
        state.year.toInt() !in 1886..2100 -> context.getString(R.string.vehicle_validation_year_range)
        else -> null
    }
    val vinError: String? = when {
        state.vin.isBlank() -> null
        state.vin.length != 17 -> context.getString(R.string.vehicle_validation_vin_length)
        else -> null
    }
    val doorsError: String? = when (state.vehicleType) {
        VehicleTypeUi.Motorcycle -> null
        VehicleTypeUi.Car -> when {
            state.doors.isBlank() -> null
            state.doors.toIntOrNull() == null -> context.getString(R.string.vehicle_validation_doors_numeric)
            state.doors.toInt() <= 0 -> context.getString(R.string.vehicle_validation_doors_positive)
            else -> null
        }
    }
    val odometerError: String? = when {
        state.currentOdometerKm.isBlank() -> context.getString(R.string.vehicle_validation_odometer_required)
        state.currentOdometerKm.toLongOrNull() == null ->
            context.getString(R.string.vehicle_validation_odometer_numeric)
        state.currentOdometerKm.toLong() < 0L ->
            context.getString(R.string.vehicle_validation_odometer_negative)
        else -> null
    }
    val odometerReminderIntervalError: String? = when {
        !state.odometerReminderEnabled -> null
        state.odometerReminderIntervalValue.isBlank() ->
            context.getString(R.string.vehicle_validation_reminder_interval_required)
        state.odometerReminderIntervalValue.toIntOrNull() == null ->
            context.getString(R.string.vehicle_validation_reminder_interval_numeric)
        state.odometerReminderIntervalValue.toInt() <= 0 ->
            context.getString(R.string.vehicle_validation_reminder_interval_positive)
        else -> null
    }

    val errors = VehicleFormErrors(
        name = nameError,
        brand = brandError,
        model = modelError,
        year = yearError,
        vin = vinError,
        doors = doorsError,
        currentOdometerKm = odometerError,
        odometerReminderIntervalValue = odometerReminderIntervalError,
    )
    val isValid: Boolean = listOf(
        errors.name,
        errors.brand,
        errors.model,
        errors.year,
        errors.vin,
        errors.doors,
        errors.currentOdometerKm,
        errors.odometerReminderIntervalValue,
    ).all { error: String? -> error == null }

    return VehicleFormValidationResult(
        isValid = isValid,
        errors = errors,
    )
}
