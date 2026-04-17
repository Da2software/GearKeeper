package com.example.gearkeeper.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.gearkeeper.R

enum class AppDestination(
    val titleRes: Int,
    val icon: ImageVector,
) {
    PlannedMaintenance(
        titleRes = R.string.nav_planned_maintenance,
        icon = Icons.Filled.CalendarMonth,
    ),
    Vehicles(
        titleRes = R.string.nav_vehicles,
        icon = Icons.Filled.DirectionsCar,
    ),
    Services(
        titleRes = R.string.nav_service_catalog,
        icon = Icons.Filled.Build,
    ),
    MaintenanceHistory(
        titleRes = R.string.nav_maintenance_history,
        icon = Icons.Filled.History,
    ),
    ReplacedParts(
        titleRes = R.string.nav_replaced_parts,
        icon = Icons.Filled.HomeRepairService,
    ),
    Settings(
        titleRes = R.string.nav_settings,
        icon = Icons.Filled.Settings,
    ),
    DatabaseHealth(
        titleRes = R.string.nav_database_health,
        icon = Icons.Filled.Storage,
    ),
}
