package com.example.gearkeeper.ui.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.DatabaseBackupRepository
import com.example.gearkeeper.data.local.DatabaseStartupAssessment
import com.example.gearkeeper.data.local.DatabaseStartupChecker
import com.example.gearkeeper.data.local.GearKeeperDatabaseHelper
import com.example.gearkeeper.data.local.GearKeeperDatabaseSingleton
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed class DatabaseGateState {
    data object Loading : DatabaseGateState()

    data object Ready : DatabaseGateState()

    data class Blocked(
        val assessment: DatabaseStartupAssessment,
    ) : DatabaseGateState()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GearKeeperNavigationShell(
    pendingOpenRoute: String? = null,
    onPendingOpenRouteConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backupRepository: DatabaseBackupRepository = remember(context) {
        DatabaseBackupRepository(context = context)
    }
    var gate: DatabaseGateState by remember { mutableStateOf(DatabaseGateState.Loading) }
    var pendingBackupThenWipe: Boolean by remember { mutableStateOf(false) }

    val recoverBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType = "application/octet-stream"),
        onResult = { uri ->
            if (pendingBackupThenWipe) {
                pendingBackupThenWipe = false
                if (uri != null) {
                    coroutineScope.launch {
                        val dbFile = context.getDatabasePath(GearKeeperDatabaseHelper.DATABASE_FILE_NAME)
                        val copyResult: Result<Unit> = withContext(Dispatchers.IO) {
                            backupRepository.exportSourceFileToUri(sourceFile = dbFile, uri = uri)
                        }
                        copyResult.onFailure { error: Throwable ->
                            Toast.makeText(
                                context,
                                error.message ?: context.getString(R.string.settings_export_failed),
                                Toast.LENGTH_LONG,
                            ).show()
                            return@launch
                        }
                        withContext(Dispatchers.IO) {
                            wipeLocalDatabase(context = context.applicationContext)
                        }
                        gate = DatabaseGateState.Ready
                        Toast.makeText(
                            context,
                            context.getString(R.string.db_startup_reset_done),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.db_startup_backup_cancelled),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        },
    )

    LaunchedEffect(Unit) {
        val outcome: DatabaseStartupAssessment = withContext(Dispatchers.IO) {
            DatabaseStartupChecker.assessBeforeOpen(context = context.applicationContext)
        }
        gate = when (outcome) {
            DatabaseStartupAssessment.Ok -> DatabaseGateState.Ready
            is DatabaseStartupAssessment.IntegrityFailed -> DatabaseGateState.Blocked(assessment = outcome)
            is DatabaseStartupAssessment.SchemaTooNew -> DatabaseGateState.Blocked(assessment = outcome)
        }
    }

    when (val state: DatabaseGateState = gate) {
        DatabaseGateState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is DatabaseGateState.Blocked -> {
            val body: String = when (val a: DatabaseStartupAssessment = state.assessment) {
                is DatabaseStartupAssessment.IntegrityFailed -> {
                    context.getString(R.string.db_startup_integrity_body, a.detail)
                }
                is DatabaseStartupAssessment.SchemaTooNew -> {
                    context.getString(R.string.db_startup_schema_newer_body, a.fileUserVersion)
                }
                DatabaseStartupAssessment.Ok -> ""
            }
            AlertDialog(
                onDismissRequest = { },
                title = { Text(text = stringResource(id = R.string.db_startup_integrity_title)) },
                text = {
                    Column {
                        Text(text = body, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.settings_sync_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingBackupThenWipe = true
                            val suggested: String = "gk-${LocalDate.now()}-recover.db"
                            recoverBackupLauncher.launch(suggested)
                        },
                    ) {
                        Text(text = stringResource(id = R.string.db_startup_backup_reset))
                    }
                },
                dismissButton = {
                    Column {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        wipeLocalDatabase(context = context.applicationContext)
                                    }
                                    gate = DatabaseGateState.Ready
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.db_startup_reset_done),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                        ) {
                            Text(text = stringResource(id = R.string.db_startup_reset_only))
                        }
                        TextButton(
                            onClick = {
                                (context as? android.app.Activity)?.finishAffinity()
                            },
                        ) {
                            Text(text = stringResource(id = R.string.db_startup_exit))
                        }
                    }
                },
            )
        }
        DatabaseGateState.Ready -> {
            ShellContent(
                pendingOpenRoute = pendingOpenRoute,
                onPendingOpenRouteConsumed = onPendingOpenRouteConsumed,
                modifier = modifier,
            )
        }
    }
}

private fun wipeLocalDatabase(context: android.content.Context): Unit {
    GearKeeperDatabaseSingleton.closeAndReset()
    context.deleteDatabase(GearKeeperDatabaseHelper.DATABASE_FILE_NAME)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ShellContent(
    pendingOpenRoute: String? = null,
    onPendingOpenRouteConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
): Unit {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String? = navBackStackEntry?.destination?.route

    val showBack: Boolean = currentRoute != null &&
        (
            currentRoute.startsWith(prefix = "vehicle_maintenances/") ||
                currentRoute.startsWith(prefix = "maintenance_form/") ||
                currentRoute.startsWith(prefix = "maintenance_edit/") ||
                currentRoute == NavRoutes.ODOMETER_REMINDERS ||
                currentRoute.startsWith(prefix = "planned_maintenance_form/") ||
                currentRoute.startsWith(prefix = "planned_maintenance_edit/") ||
                currentRoute == NavRoutes.CATALOG_ADD_SERVICE_TYPE ||
                currentRoute.startsWith(prefix = "catalog_edit_service_type/")
            ) &&
        navController.previousBackStackEntry != null

    LaunchedEffect(pendingOpenRoute) {
        val route: String = pendingOpenRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            launchSingleTop = true
        }
        onPendingOpenRouteConsumed()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(id = R.string.nav_brand_name),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                )
                AppDestination.entries.forEach { destination: AppDestination ->
                    val label: String = stringResource(id = destination.titleRes)
                    NavigationDrawerItem(
                        selected = isDrawerDestinationSelected(
                            destination = destination,
                            currentRoute = currentRoute,
                        ),
                        onClick = {
                            navController.navigate(route = destination.toNavRoute()) {
                                popUpTo(route = NavRoutes.PLANNED_MAINTENANCE) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        },
                        label = { Text(text = label) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = label,
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = navTitleResForRoute(route = currentRoute)))
                    },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(
                                onClick = {
                                    navController.navigateUp()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.cd_back),
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = stringResource(id = R.string.cd_open_menu),
                                )
                            }
                        }
                    },
                )
            },
        ) { innerPadding ->
            GearKeeperNavHost(
                navController = navController,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
        }
    }
}
