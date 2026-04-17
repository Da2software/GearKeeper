package com.example.gearkeeper.ui.navigation

import androidx.annotation.StringRes
import com.example.gearkeeper.R

object NavRoutes {
    /** Use as [maintenanceForm] `vehicleId` to open the form with a vehicle dropdown. */
    const val MAINTENANCE_VEHICLE_PICKER_ID: Long = -1L

    const val INCOMING: String = "incoming"
    const val PLANNED_MAINTENANCE: String = "planned_maintenance"
    const val PLANNED_MAINTENANCE_FORM: String = "planned_maintenance_form/{vehicleId}"
    const val PLANNED_MAINTENANCE_EDIT: String = "planned_maintenance_edit/{plannedId}"
    const val VEHICLES: String = "vehicles"
    const val SERVICES: String = "services"
    const val MAINTENANCE_HISTORY: String = "maintenance_history"
    const val REPLACED_PARTS: String = "replaced_parts"
    const val SETTINGS: String = "settings"
    const val DATABASE_HEALTH: String = "database_health"

    const val VEHICLE_MAINTENANCES: String = "vehicle_maintenances/{vehicleId}"
    const val MAINTENANCE_FORM: String = "maintenance_form/{vehicleId}/{presetServiceTypeId}"
    const val MAINTENANCE_EDIT: String = "maintenance_edit/{maintenanceId}"
    const val ODOMETER_REMINDERS: String = "odometer_reminders"
    const val CATALOG_ADD_SERVICE_TYPE: String = "catalog_add_service_type"
    const val CATALOG_EDIT_SERVICE_TYPE: String = "catalog_edit_service_type/{serviceTypeId}"

    val TOP_LEVEL_ROUTES: Set<String> = setOf(
        INCOMING,
        PLANNED_MAINTENANCE,
        VEHICLES,
        SERVICES,
        MAINTENANCE_HISTORY,
        REPLACED_PARTS,
        SETTINGS,
        DATABASE_HEALTH,
    )

    fun vehicleMaintenances(vehicleId: Long): String = "vehicle_maintenances/$vehicleId"

    fun plannedMaintenanceForm(vehicleId: Long): String = "planned_maintenance_form/$vehicleId"

    fun plannedMaintenanceEdit(plannedId: Long): String = "planned_maintenance_edit/$plannedId"

    fun maintenanceForm(vehicleId: Long, presetServiceTypeId: Long): String =
        "maintenance_form/$vehicleId/$presetServiceTypeId"

    fun maintenanceEdit(maintenanceId: Long): String = "maintenance_edit/$maintenanceId"

    fun catalogEditServiceType(serviceTypeId: Long): String =
        "catalog_edit_service_type/$serviceTypeId"
}

fun AppDestination.toNavRoute(): String {
    return when (this) {
        AppDestination.PlannedMaintenance -> NavRoutes.PLANNED_MAINTENANCE
        AppDestination.Vehicles -> NavRoutes.VEHICLES
        AppDestination.Services -> NavRoutes.SERVICES
        AppDestination.MaintenanceHistory -> NavRoutes.MAINTENANCE_HISTORY
        AppDestination.ReplacedParts -> NavRoutes.REPLACED_PARTS
        AppDestination.Settings -> NavRoutes.SETTINGS
        AppDestination.DatabaseHealth -> NavRoutes.DATABASE_HEALTH
    }
}

@StringRes
fun navTitleResForRoute(route: String?): Int {
    if (route == null) {
        return R.string.app_name
    }
    return when {
        route == NavRoutes.INCOMING -> AppDestination.PlannedMaintenance.titleRes
        route == NavRoutes.PLANNED_MAINTENANCE -> AppDestination.PlannedMaintenance.titleRes
        route.startsWith("planned_maintenance_form/") -> R.string.title_planned_maintenance_new
        route.startsWith("planned_maintenance_edit/") -> R.string.title_planned_maintenance_edit
        route == NavRoutes.VEHICLES -> AppDestination.Vehicles.titleRes
        route == NavRoutes.SERVICES -> AppDestination.Services.titleRes
        route == NavRoutes.MAINTENANCE_HISTORY -> AppDestination.MaintenanceHistory.titleRes
        route == NavRoutes.REPLACED_PARTS -> AppDestination.ReplacedParts.titleRes
        route == NavRoutes.SETTINGS -> AppDestination.Settings.titleRes
        route == NavRoutes.DATABASE_HEALTH -> AppDestination.DatabaseHealth.titleRes
        route.startsWith("vehicle_maintenances/") -> R.string.title_vehicle_maintenance
        route.startsWith("maintenance_form/") -> R.string.title_log_maintenance
        route.startsWith("maintenance_edit/") -> R.string.title_edit_maintenance
        route == NavRoutes.ODOMETER_REMINDERS -> R.string.title_odometer_reminders
        route == NavRoutes.CATALOG_ADD_SERVICE_TYPE -> R.string.title_new_service_type
        route.startsWith("catalog_edit_service_type/") -> R.string.title_edit_service_type
        else -> R.string.app_name
    }
}

fun isDrawerDestinationSelected(
    destination: AppDestination,
    currentRoute: String?,
): Boolean {
    if (currentRoute == null) {
        return false
    }
    return when (destination) {
        AppDestination.PlannedMaintenance -> currentRoute == NavRoutes.PLANNED_MAINTENANCE
            || currentRoute == NavRoutes.INCOMING
        AppDestination.Vehicles ->
            currentRoute == NavRoutes.VEHICLES ||
                currentRoute.startsWith("vehicle_maintenances/") ||
                currentRoute.startsWith("maintenance_form/") ||
                currentRoute.startsWith("maintenance_edit/") ||
                currentRoute == NavRoutes.ODOMETER_REMINDERS ||
                currentRoute.startsWith("planned_maintenance_form/") ||
                currentRoute.startsWith("planned_maintenance_edit/")
        AppDestination.Services ->
            currentRoute == NavRoutes.SERVICES ||
                currentRoute == NavRoutes.CATALOG_ADD_SERVICE_TYPE ||
                currentRoute.startsWith("catalog_edit_service_type/")
        AppDestination.MaintenanceHistory -> currentRoute == NavRoutes.MAINTENANCE_HISTORY
        AppDestination.ReplacedParts -> currentRoute == NavRoutes.REPLACED_PARTS
        AppDestination.Settings -> currentRoute == NavRoutes.SETTINGS
        AppDestination.DatabaseHealth -> currentRoute == NavRoutes.DATABASE_HEALTH
    }
}
