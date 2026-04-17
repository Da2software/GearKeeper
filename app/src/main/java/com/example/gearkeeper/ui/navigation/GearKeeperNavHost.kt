package com.example.gearkeeper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.gearkeeper.data.preferences.UserPreferences
import com.example.gearkeeper.data.preferences.UserPreferencesRepository
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gearkeeper.data.local.MaintenanceRepository
import com.example.gearkeeper.data.local.OdometerRepository
import com.example.gearkeeper.data.local.PlannedMaintenanceRepository
import com.example.gearkeeper.data.local.ReplacedPartRepository
import com.example.gearkeeper.data.local.ServiceTypeRepository
import com.example.gearkeeper.data.local.VehicleRepository
import com.example.gearkeeper.data.local.OdometerReminderRepository
import com.example.gearkeeper.ui.screens.diagnostics.DatabaseHealthScreen
import com.example.gearkeeper.ui.screens.history.MaintenanceHistoryScreen
import com.example.gearkeeper.ui.screens.parts.ReplacedPartsListScreen
import com.example.gearkeeper.ui.screens.planned.PlannedMaintenanceFormScreen
import com.example.gearkeeper.ui.screens.planned.PlannedMaintenanceListScreen
import com.example.gearkeeper.ui.screens.services.MaintenanceFormScreen
import com.example.gearkeeper.ui.screens.services.ServiceTypeEditorScreen
import com.example.gearkeeper.ui.screens.services.ServicesHomeScreen
import com.example.gearkeeper.ui.screens.services.VehicleMaintenancesScreen
import com.example.gearkeeper.ui.screens.settings.SettingsScreen
import com.example.gearkeeper.ui.screens.vehicles.VehiclesScreen
import com.example.gearkeeper.ui.screens.vehicles.OdometerReminderPanelScreen

@Composable
fun GearKeeperNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    val vehicleRepository: VehicleRepository = remember(context) {
        VehicleRepository(context = context)
    }
    val maintenanceRepository: MaintenanceRepository = remember(context) {
        MaintenanceRepository(context = context)
    }
    val serviceTypeRepository: ServiceTypeRepository = remember(context) {
        ServiceTypeRepository(context = context)
    }
    val odometerRepository: OdometerRepository = remember(context) {
        OdometerRepository(context = context)
    }
    val plannedMaintenanceRepository: PlannedMaintenanceRepository = remember(context) {
        PlannedMaintenanceRepository(context = context)
    }
    val replacedPartRepository: ReplacedPartRepository = remember(context) {
        ReplacedPartRepository(context = context)
    }
    val odometerReminderRepository: OdometerReminderRepository = remember(context) {
        OdometerReminderRepository(context = context)
    }
    val userPreferencesRepository: UserPreferencesRepository = remember(context) {
        UserPreferencesRepository(context = context)
    }
    val userPrefs: UserPreferences by userPreferencesRepository.userPreferences.collectAsState(
        initial = UserPreferences.default,
    )

    CompositionLocalProvider(LocalDistanceUnit provides userPrefs.distanceUnit) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.PLANNED_MAINTENANCE,
            modifier = modifier,
        ) {
        composable(route = NavRoutes.INCOMING) {
            PlannedMaintenanceListScreen(
                plannedMaintenanceRepository = plannedMaintenanceRepository,
                onAddPlanned = {
                    navController.navigate(
                        NavRoutes.plannedMaintenanceForm(vehicleId = NavRoutes.MAINTENANCE_VEHICLE_PICKER_ID),
                    )
                },
                onOpenMaintenance = { vehicleId: Long, presetServiceTypeId: Long ->
                    navController.navigate(
                        NavRoutes.maintenanceForm(
                            vehicleId = vehicleId,
                            presetServiceTypeId = presetServiceTypeId,
                        ),
                    )
                },
                onEditPlanned = { plannedId: Long ->
                    navController.navigate(NavRoutes.plannedMaintenanceEdit(plannedId = plannedId))
                },
                modifier = Modifier,
            )
        }
        composable(route = NavRoutes.PLANNED_MAINTENANCE) {
            PlannedMaintenanceListScreen(
                plannedMaintenanceRepository = plannedMaintenanceRepository,
                onAddPlanned = {
                    navController.navigate(
                        NavRoutes.plannedMaintenanceForm(vehicleId = NavRoutes.MAINTENANCE_VEHICLE_PICKER_ID),
                    )
                },
                onOpenMaintenance = { vehicleId: Long, presetServiceTypeId: Long ->
                    navController.navigate(
                        NavRoutes.maintenanceForm(
                            vehicleId = vehicleId,
                            presetServiceTypeId = presetServiceTypeId,
                        ),
                    )
                },
                onEditPlanned = { plannedId: Long ->
                    navController.navigate(NavRoutes.plannedMaintenanceEdit(plannedId = plannedId))
                },
            )
        }
        composable(
            route = NavRoutes.PLANNED_MAINTENANCE_FORM,
            arguments = listOf(
                navArgument(name = "vehicleId") {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val formVehicleId: Long = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            PlannedMaintenanceFormScreen(
                vehicleId = formVehicleId,
                vehicleRepository = vehicleRepository,
                serviceTypeRepository = serviceTypeRepository,
                plannedMaintenanceRepository = plannedMaintenanceRepository,
                onNavigateToAddServiceType = {
                    navController.navigate(NavRoutes.CATALOG_ADD_SERVICE_TYPE)
                },
                onSaved = {
                    navController.navigateUp()
                },
            )
        }
        composable(
            route = NavRoutes.PLANNED_MAINTENANCE_EDIT,
            arguments = listOf(
                navArgument(name = "plannedId") {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val plannedId: Long = backStackEntry.arguments?.getLong("plannedId") ?: return@composable
            PlannedMaintenanceFormScreen(
                vehicleId = 0L,
                vehicleRepository = vehicleRepository,
                serviceTypeRepository = serviceTypeRepository,
                plannedMaintenanceRepository = plannedMaintenanceRepository,
                onNavigateToAddServiceType = {
                    navController.navigate(NavRoutes.CATALOG_ADD_SERVICE_TYPE)
                },
                onSaved = {
                    navController.navigateUp()
                },
                editingPlannedMaintenanceId = plannedId,
            )
        }
        composable(route = NavRoutes.VEHICLES) {
            VehiclesScreen(
                onOpenVehicleMaintenances = { vehicleId: Long ->
                    navController.navigate(NavRoutes.vehicleMaintenances(vehicleId = vehicleId))
                },
                onLogMaintenanceForVehicle = { vehicleId: Long ->
                    navController.navigate(
                        NavRoutes.maintenanceForm(vehicleId = vehicleId, presetServiceTypeId = -1L),
                    )
                },
                onPlanFutureMaintenanceForVehicle = { vehicleId: Long ->
                    navController.navigate(NavRoutes.plannedMaintenanceForm(vehicleId = vehicleId))
                },
                onAddMaintenancePickVehicle = {
                    navController.navigate(
                        NavRoutes.maintenanceForm(
                            vehicleId = NavRoutes.MAINTENANCE_VEHICLE_PICKER_ID,
                            presetServiceTypeId = -1L,
                        ),
                    )
                },
                onOpenOdometerReminders = {
                    navController.navigate(NavRoutes.ODOMETER_REMINDERS)
                },
            )
        }
        composable(route = NavRoutes.ODOMETER_REMINDERS) {
            OdometerReminderPanelScreen(
                odometerReminderRepository = odometerReminderRepository,
                onOpenVehicleMaintenances = { vehicleId: Long ->
                    navController.navigate(NavRoutes.vehicleMaintenances(vehicleId = vehicleId))
                },
            )
        }
        composable(route = NavRoutes.SERVICES) {
            ServicesHomeScreen(
                serviceTypeRepository = serviceTypeRepository,
                onAddCustomService = {
                    navController.navigate(NavRoutes.CATALOG_ADD_SERVICE_TYPE)
                },
                onEditCustomService = { serviceTypeId: Long ->
                    navController.navigate(NavRoutes.catalogEditServiceType(serviceTypeId = serviceTypeId))
                },
            )
        }
        composable(route = NavRoutes.CATALOG_ADD_SERVICE_TYPE) {
            ServiceTypeEditorScreen(
                serviceTypeRepository = serviceTypeRepository,
                serviceTypeId = null,
                onFinished = {
                    navController.navigateUp()
                },
            )
        }
        composable(
            route = NavRoutes.CATALOG_EDIT_SERVICE_TYPE,
            arguments = listOf(
                navArgument(name = "serviceTypeId") {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val editId: Long = backStackEntry.arguments?.getLong("serviceTypeId") ?: return@composable
            ServiceTypeEditorScreen(
                serviceTypeRepository = serviceTypeRepository,
                serviceTypeId = editId,
                onFinished = {
                    navController.navigateUp()
                },
            )
        }
        composable(
            route = NavRoutes.VEHICLE_MAINTENANCES,
            arguments = listOf(
                navArgument(name = "vehicleId") {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val vehicleId: Long = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            VehicleMaintenancesScreen(
                vehicleId = vehicleId,
                vehicleRepository = vehicleRepository,
                maintenanceRepository = maintenanceRepository,
                odometerRepository = odometerRepository,
                onAddMaintenance = {
                    navController.navigate(
                        NavRoutes.maintenanceForm(vehicleId = vehicleId, presetServiceTypeId = -1L),
                    )
                },
                onEditMaintenance = { maintenanceId: Long ->
                    navController.navigate(NavRoutes.maintenanceEdit(maintenanceId = maintenanceId))
                },
                onPlanFutureMaintenance = {
                    navController.navigate(NavRoutes.plannedMaintenanceForm(vehicleId = vehicleId))
                },
            )
        }
        composable(
            route = NavRoutes.MAINTENANCE_EDIT,
            arguments = listOf(
                navArgument(name = "maintenanceId") {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val maintenanceId: Long = backStackEntry.arguments?.getLong("maintenanceId") ?: return@composable
            MaintenanceFormScreen(
                vehicleId = NavRoutes.MAINTENANCE_VEHICLE_PICKER_ID,
                presetServiceTypeId = -1L,
                vehicleRepository = vehicleRepository,
                serviceTypeRepository = serviceTypeRepository,
                maintenanceRepository = maintenanceRepository,
                replacedPartRepository = replacedPartRepository,
                odometerRepository = odometerRepository,
                onNavigateToAddServiceType = {
                    navController.navigate(NavRoutes.CATALOG_ADD_SERVICE_TYPE)
                },
                onSaved = {
                    navController.navigateUp()
                },
                editingMaintenanceId = maintenanceId,
            )
        }
        composable(
            route = NavRoutes.MAINTENANCE_FORM,
            arguments = listOf(
                navArgument(name = "vehicleId") {
                    type = NavType.LongType
                },
                navArgument(name = "presetServiceTypeId") {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val vehicleId: Long = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val presetServiceTypeId: Long =
                backStackEntry.arguments?.getLong("presetServiceTypeId") ?: -1L
            MaintenanceFormScreen(
                vehicleId = vehicleId,
                presetServiceTypeId = presetServiceTypeId,
                vehicleRepository = vehicleRepository,
                serviceTypeRepository = serviceTypeRepository,
                maintenanceRepository = maintenanceRepository,
                replacedPartRepository = replacedPartRepository,
                odometerRepository = odometerRepository,
                onNavigateToAddServiceType = {
                    navController.navigate(NavRoutes.CATALOG_ADD_SERVICE_TYPE)
                },
                onSaved = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = NavRoutes.MAINTENANCE_HISTORY) {
            MaintenanceHistoryScreen()
        }
        composable(route = NavRoutes.REPLACED_PARTS) {
            ReplacedPartsListScreen(
                replacedPartRepository = replacedPartRepository,
                vehicleRepository = vehicleRepository,
            )
        }
        composable(route = NavRoutes.SETTINGS) {
            SettingsScreen(
                userPreferencesRepository = userPreferencesRepository,
            )
        }
        composable(route = NavRoutes.DATABASE_HEALTH) {
            DatabaseHealthScreen()
        }
        }
    }
}
