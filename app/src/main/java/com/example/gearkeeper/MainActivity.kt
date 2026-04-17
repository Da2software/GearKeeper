package com.example.gearkeeper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.gearkeeper.reminders.OdometerReminderNotification
import com.example.gearkeeper.ui.navigation.GearKeeperNavigationShell
import com.example.gearkeeper.ui.theme.GearKeeperTheme

class MainActivity : AppCompatActivity() {
    private val pendingOpenRouteState = mutableStateOf<String?>(null)
    private val requestNotificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _: Boolean ->
        // No-op: reminder workers will re-check permission before posting notifications.
    }

    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        pendingOpenRouteState.value = intent?.getStringExtra(OdometerReminderNotification.EXTRA_OPEN_ROUTE)
        ensureNotificationsPermissionIfNeeded()
        setContent {
            GearKeeperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GearKeeperNavigationShell(
                        pendingOpenRoute = pendingOpenRouteState.value,
                        onPendingOpenRouteConsumed = { pendingOpenRouteState.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?): Unit {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingOpenRouteState.value = intent?.getStringExtra(OdometerReminderNotification.EXTRA_OPEN_ROUTE)
    }

    private fun ensureNotificationsPermissionIfNeeded(): Unit {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (OdometerReminderNotification.areNotificationsAllowed(context = this)) {
            return
        }
        val permissionState: Int = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            requestNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}