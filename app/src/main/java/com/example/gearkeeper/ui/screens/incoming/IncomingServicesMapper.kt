package com.example.gearkeeper.ui.screens.incoming

import com.example.gearkeeper.data.local.PlannedMaintenanceIncomingRow
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.domain.planned.PlannedMaintenanceLifecycleStatus
import com.example.gearkeeper.domain.planned.PlannedMaintenanceStatusResolver
import com.example.gearkeeper.domain.planned.PlannedScheduleMode
import com.example.gearkeeper.domain.preferences.DistanceUnit
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

internal fun todayLocalDate(): LocalDate {
    return LocalDate.now(ZoneId.systemDefault())
}

internal fun PlannedMaintenanceIncomingRow.toIncomingServiceUiModel(
    distanceUnit: DistanceUnit,
    today: LocalDate,
    expiresDateTemplate: String,
    reminderDateTemplate: String,
    expiresOdometerTemplate: String,
    reminderOdometerTemplate: String,
): IncomingServiceUiModel {
    val lifecycle: PlannedMaintenanceLifecycleStatus = PlannedMaintenanceStatusResolver.resolve(
        mode = scheduleMode,
        targetDateIso = targetDate,
        leadDays = leadDays,
        targetOdometerKm = targetOdometerKm,
        leadOdometerKm = leadOdometerKm,
        currentOdometerKm = currentOdometerKm,
        today = today,
    )
    val status: IncomingServiceStatus = when (lifecycle) {
        PlannedMaintenanceLifecycleStatus.SCHEDULED -> IncomingServiceStatus.Scheduled
        PlannedMaintenanceLifecycleStatus.REMINDER_WINDOW -> IncomingServiceStatus.ReminderWindow
        PlannedMaintenanceLifecycleStatus.OVERDUE -> IncomingServiceStatus.Overdue
    }
    val expiresAndReminder: Pair<String, String> = when (scheduleMode) {
        PlannedScheduleMode.DATE -> {
            val target: LocalDate = parseIsoDate(value = targetDate) ?: today
            val reminderFrom: LocalDate = target.minusDays(leadDays.toLong().coerceAtLeast(0L))
            Pair(
                String.format(expiresDateTemplate, target.toString()),
                String.format(reminderDateTemplate, reminderFrom.toString()),
            )
        }
        PlannedScheduleMode.ODOMETER -> {
            val targetKm: Int = targetOdometerKm ?: 0
            val leadKm: Int = leadOdometerKm.coerceAtLeast(0).coerceAtMost(targetKm)
            val windowStartKm: Int = (targetKm.toLong() - leadKm.toLong()).toInt().coerceAtLeast(0)
            val targetLabel: String = OdometerUnitConverter.formatStoredKm(
                km = targetKm,
                unit = distanceUnit,
            )
            val windowLabel: String = OdometerUnitConverter.formatStoredKm(
                km = windowStartKm,
                unit = distanceUnit,
            )
            Pair(
                String.format(expiresOdometerTemplate, targetLabel),
                String.format(reminderOdometerTemplate, windowLabel),
            )
        }
    }
    val expiresLine: String = expiresAndReminder.first
    val reminderLine: String = expiresAndReminder.second
    val brandModelLine: String = listOf(vehicleBrandName.trim(), vehicleModelName.trim())
        .filter { part: String -> part.isNotEmpty() }
        .joinToString(separator = " ")
    return IncomingServiceUiModel(
        plannedMaintenanceId = id,
        vehicleId = vehicleId,
        vehicleName = vehicleName,
        vehicleBrandModelLine = brandModelLine,
        planTitle = title.trim().ifEmpty { vehicleName },
        expiresLine = expiresLine,
        reminderLine = reminderLine,
        status = status,
        presetServiceTypeId = firstPresetServiceTypeId,
    )
}

internal fun compareIncomingByUrgency(a: IncomingServiceUiModel, b: IncomingServiceUiModel): Int {
    val rank: (IncomingServiceUiModel) -> Int = { m: IncomingServiceUiModel ->
        when (m.status) {
            IncomingServiceStatus.Overdue -> 0
            IncomingServiceStatus.ReminderWindow -> 1
            IncomingServiceStatus.Scheduled -> 2
        }
    }
    val diff: Int = rank(a) - rank(b)
    if (diff != 0) {
        return diff
    }
    return a.planTitle.compareTo(other = b.planTitle, ignoreCase = true)
}

private fun parseIsoDate(value: String?): LocalDate? {
    if (value == null) {
        return null
    }
    val trimmed: String = value.trim()
    if (trimmed.length < 10) {
        return null
    }
    val head: String = trimmed.substring(0, 10)
    return try {
        LocalDate.parse(head)
    } catch (_: DateTimeParseException) {
        null
    }
}
