package com.example.gearkeeper.ui.screens.settings

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.DatabaseBackupRepository
import com.example.gearkeeper.data.preferences.AppLocaleCoordinator
import com.example.gearkeeper.data.preferences.UserPreferences
import com.example.gearkeeper.data.preferences.UserPreferencesRepository
import com.example.gearkeeper.domain.preferences.DistanceUnit
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import java.time.LocalDate
import kotlinx.coroutines.launch

private data class LanguageOption(
    val tag: String,
    val labelRes: Int,
)

@Composable
fun SettingsScreen(
    userPreferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier,
): Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupRepository: DatabaseBackupRepository = remember(context) {
        DatabaseBackupRepository(context = context)
    }

    val prefs: UserPreferences by userPreferencesRepository.userPreferences.collectAsState(
        initial = UserPreferences.default,
    )

    val languageOptions: List<LanguageOption> = remember {
        listOf(
            LanguageOption(tag = "", labelRes = R.string.settings_language_system),
            LanguageOption(tag = "en", labelRes = R.string.settings_language_english),
            LanguageOption(tag = "es", labelRes = R.string.settings_language_spanish),
        )
    }

    val distanceOptions: List<Pair<DistanceUnit, Int>> = remember {
        listOf(
            DistanceUnit.METRIC_KM to R.string.settings_distance_km,
            DistanceUnit.IMPERIAL_MILES to R.string.settings_distance_mi,
        )
    }

    val defaultExportFileName: String = remember {
        "gk-${LocalDate.now()}.db"
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType = "application/octet-stream"),
        onResult = { uri ->
            if (uri != null) {
                val outcome: Result<Unit> = backupRepository.exportToUri(uri = uri)
                outcome.onSuccess {
                    Toast.makeText(context, context.getString(R.string.settings_export_success), Toast.LENGTH_SHORT)
                        .show()
                }.onFailure { error: Throwable ->
                    Toast.makeText(
                        context,
                        error.message ?: context.getString(R.string.settings_export_failed),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        },
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                val outcome: Result<Unit> = backupRepository.importFromUri(uri = uri)
                outcome.onSuccess {
                    Toast.makeText(context, context.getString(R.string.settings_import_success), Toast.LENGTH_SHORT)
                        .show()
                    (context as? Activity)?.recreate()
                }.onFailure { error: Throwable ->
                    Toast.makeText(
                        context,
                        error.message ?: context.getString(R.string.settings_import_failed),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = ListScreenHorizontalPadding,
                vertical = ListScreenVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(ListScreenSectionSpacing),
    ) {
        Text(
            text = stringResource(id = R.string.settings_language_section),
            style = MaterialTheme.typography.titleMedium,
        )
        languageOptions.forEach { option: LanguageOption ->
            val selected: Boolean = option.tag == prefs.languageTag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        onClick = {
                            scope.launch {
                                userPreferencesRepository.setLanguageTag(tag = option.tag)
                                AppLocaleCoordinator.apply(languageTag = option.tag)
                                (context as? Activity)?.recreate()
                            }
                        },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                )
                Text(
                    text = stringResource(id = option.labelRes),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        Text(
            text = stringResource(id = R.string.settings_distance_section),
            style = MaterialTheme.typography.titleMedium,
        )
        distanceOptions.forEach { pair: Pair<DistanceUnit, Int> ->
            val unit: DistanceUnit = pair.first
            val selected: Boolean = prefs.distanceUnit == unit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        onClick = {
                            scope.launch {
                                userPreferencesRepository.setDistanceUnit(unit = unit)
                            }
                        },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                )
                Text(
                    text = stringResource(id = pair.second),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        Text(
            text = stringResource(id = R.string.settings_sync_section),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(id = R.string.settings_sync_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = {
                exportLauncher.launch(defaultExportFileName)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.settings_export))
        }
        Button(
            onClick = {
                importLauncher.launch("*/*")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.settings_import))
        }
    }
}
