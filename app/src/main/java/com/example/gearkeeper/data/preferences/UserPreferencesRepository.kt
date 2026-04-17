package com.example.gearkeeper.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gearkeeper.domain.preferences.DistanceUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

/**
 * Persists user settings with Jetpack **DataStore Preferences** (files under the application private storage).
 * Values survive process death and reboots; this is the recommended Android approach (typed, async, migration-friendly).
 * Distance and language are not duplicated in SQLite to avoid two sources of truth.
 */
class UserPreferencesRepository(
    context: Context,
) {
    private val appContext: Context = context.applicationContext

    val userPreferences: Flow<UserPreferences> = appContext.userPreferencesDataStore.data.map { prefs: Preferences ->
        val lang: String = prefs[LANGUAGE_TAG_KEY].orEmpty()
        val unitRaw: String = prefs[DISTANCE_UNIT_KEY] ?: DistanceUnit.METRIC_KM.name
        val unit: DistanceUnit = distanceUnitFromStored(unitRaw)
        UserPreferences(languageTag = lang, distanceUnit = unit)
    }

    suspend fun setLanguageTag(tag: String): Unit {
        appContext.userPreferencesDataStore.edit { mutablePrefs: MutablePreferences ->
            mutablePrefs[LANGUAGE_TAG_KEY] = tag
        }
    }

    suspend fun setDistanceUnit(unit: DistanceUnit): Unit {
        appContext.userPreferencesDataStore.edit { mutablePrefs: MutablePreferences ->
            mutablePrefs[DISTANCE_UNIT_KEY] = unit.name
        }
    }

    private fun distanceUnitFromStored(raw: String): DistanceUnit {
        return when (raw) {
            DistanceUnit.IMPERIAL_MILES.name -> DistanceUnit.IMPERIAL_MILES
            else -> DistanceUnit.METRIC_KM
        }
    }

    companion object {
        private val LANGUAGE_TAG_KEY: Preferences.Key<String> = stringPreferencesKey("language_tag")
        private val DISTANCE_UNIT_KEY: Preferences.Key<String> = stringPreferencesKey("distance_unit")
    }
}
