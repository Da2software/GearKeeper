package com.example.gearkeeper.ui.screens.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.DatabaseHealthRepository
import com.example.gearkeeper.data.local.DatabaseHealthResult
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionListHeader

@Composable
fun DatabaseHealthScreen(
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    val repository: DatabaseHealthRepository = remember(context) {
        DatabaseHealthRepository(context = context)
    }
    var health: DatabaseHealthResult? by remember { mutableStateOf(null) }
    var refreshToken: Int by remember { mutableStateOf(0) }

    LaunchedEffect(refreshToken) {
        health = repository.getHealth()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = ListScreenHorizontalPadding,
                vertical = ListScreenVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(ListScreenSectionSpacing),
        horizontalAlignment = Alignment.Start,
    ) {
        SectionListHeader(
            title = null,
            supportingText = stringResource(id = R.string.db_health_supporting_text),
            primaryActionLabel = stringResource(id = R.string.db_health_refresh),
            onPrimaryAction = {
                refreshToken += 1
            },
        )
        val currentHealth: DatabaseHealthResult? = health
        if (currentHealth == null) {
            Text(text = stringResource(id = R.string.db_health_loading))
            return@Column
        }
        if (!currentHealth.isOk) {
            Text(
                text = stringResource(id = R.string.db_health_status_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = currentHealth.errorMessage ?: stringResource(id = R.string.db_health_unknown_error),
                style = MaterialTheme.typography.bodyMedium,
            )
            return@Column
        }
        Text(
            text = stringResource(id = R.string.db_health_status_ok),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = stringResource(id = R.string.db_health_schema_version, currentHealth.databaseVersion))
        Text(text = stringResource(id = R.string.db_health_brands_car, currentHealth.brandCarCount))
        Text(text = stringResource(id = R.string.db_health_brands_moto, currentHealth.brandMotorcycleCount))
        Text(text = stringResource(id = R.string.db_health_models, currentHealth.modelCount))
        Text(text = stringResource(id = R.string.db_health_vehicles_saved, currentHealth.vehicleCount))
        if (currentHealth.brandCarCount == 0 && currentHealth.brandMotorcycleCount == 0) {
            Text(
                text = stringResource(id = R.string.db_health_catalog_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
