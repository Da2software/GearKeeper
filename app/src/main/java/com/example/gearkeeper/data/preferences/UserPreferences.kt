package com.example.gearkeeper.data.preferences

import com.example.gearkeeper.domain.preferences.DistanceUnit

data class UserPreferences(
    /** BCP 47 tag, e.g. `en`, `es`. Empty means follow system locale. */
    val languageTag: String,
    val distanceUnit: DistanceUnit,
) {
    companion object {
        val default: UserPreferences = UserPreferences(
            languageTag = "",
            distanceUnit = DistanceUnit.METRIC_KM,
        )
    }
}
