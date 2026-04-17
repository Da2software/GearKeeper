package com.example.gearkeeper.domain.planned

import java.time.LocalDate
import java.time.format.DateTimeParseException

enum class PlannedScheduleMode {
    DATE,
    ODOMETER,
}

enum class PlanLeadTimeUnit {
    DAYS,
    WEEKS,
    MONTHS,
}

enum class PlannedMaintenanceLifecycleStatus {
    /** Before the user-defined reminder window. */
    SCHEDULED,

    /** Inside the reminder window but not yet past due. */
    REMINDER_WINDOW,

    /** Past the target date or target odometer. */
    OVERDUE,
}

object PlannedMaintenanceLeadRules {

    fun leadTimeUnitToDays(value: Int, unit: PlanLeadTimeUnit): Int {
        val v: Long = value.toLong().coerceAtLeast(0L)
        return when (unit) {
            PlanLeadTimeUnit.DAYS -> v.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            PlanLeadTimeUnit.WEEKS -> (v * 7L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            PlanLeadTimeUnit.MONTHS -> (v * 30L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }
    }
}

object PlannedMaintenanceStatusResolver {

    fun resolve(
        mode: PlannedScheduleMode,
        targetDateIso: String?,
        leadDays: Int,
        targetOdometerKm: Int?,
        leadOdometerKm: Int,
        currentOdometerKm: Int?,
        today: LocalDate,
    ): PlannedMaintenanceLifecycleStatus {
        return when (mode) {
            PlannedScheduleMode.DATE -> resolveDate(
                targetDateIso = targetDateIso,
                leadDays = leadDays,
                today = today,
            )
            PlannedScheduleMode.ODOMETER -> resolveOdometer(
                targetOdometerKm = targetOdometerKm,
                leadOdometerKm = leadOdometerKm,
                currentOdometerKm = currentOdometerKm,
            )
        }
    }

    private fun resolveDate(
        targetDateIso: String?,
        leadDays: Int,
        today: LocalDate,
    ): PlannedMaintenanceLifecycleStatus {
        val target: LocalDate =
            parseIsoDate(value = targetDateIso) ?: return PlannedMaintenanceLifecycleStatus.SCHEDULED
        val lead: Long = leadDays.toLong().coerceAtLeast(0L)
        val windowStart: LocalDate = target.minusDays(lead)
        if (!today.isBefore(target)) {
            return PlannedMaintenanceLifecycleStatus.OVERDUE
        }
        if (!today.isBefore(windowStart)) {
            return PlannedMaintenanceLifecycleStatus.REMINDER_WINDOW
        }
        return PlannedMaintenanceLifecycleStatus.SCHEDULED
    }

    /**
     * On the target date we treat as overdue (service day). Reminder window is [target - lead, target).
     */
    private fun resolveOdometer(
        targetOdometerKm: Int?,
        leadOdometerKm: Int,
        currentOdometerKm: Int?,
    ): PlannedMaintenanceLifecycleStatus {
        val target: Int = targetOdometerKm ?: return PlannedMaintenanceLifecycleStatus.SCHEDULED
        val current: Int = currentOdometerKm ?: return PlannedMaintenanceLifecycleStatus.SCHEDULED
        val lead: Int = leadOdometerKm.coerceAtLeast(0).coerceAtMost(target)
        val windowStart: Int = (target.toLong() - lead.toLong()).toInt().coerceAtLeast(0)
        return when {
            current >= target -> PlannedMaintenanceLifecycleStatus.OVERDUE
            current >= windowStart -> PlannedMaintenanceLifecycleStatus.REMINDER_WINDOW
            else -> PlannedMaintenanceLifecycleStatus.SCHEDULED
        }
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
}
