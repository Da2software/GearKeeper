package com.example.gearkeeper.ui.screens.incoming

enum class IncomingServiceStatus {
    /** Before the reminder window (calm). */
    Scheduled,

    /** Inside the user-defined reminder window before the due target. */
    ReminderWindow,

    /** Past the due date or target odometer. */
    Overdue,
}

data class IncomingServiceUiModel(
    /** Stable id for list keys (planned maintenance row id). */
    val plannedMaintenanceId: Long,
    val vehicleId: Long,
    val vehicleName: String,
    /** Brand and model from the vehicle record, e.g. "Toyota Corolla"; may be blank. */
    val vehicleBrandModelLine: String,
    /** User-entered plan title (required in DB). */
    val planTitle: String,
    /** Due target line, e.g. "Vence: 2026-04-30" or target odometer. */
    val expiresLine: String,
    /** Reminder window start line, e.g. "Aviso: 2026-04-22". */
    val reminderLine: String,
    val status: IncomingServiceStatus,
    /** If set, maintenance form can pre-select this catalog `service_type.id`. */
    val presetServiceTypeId: Long?,
)
