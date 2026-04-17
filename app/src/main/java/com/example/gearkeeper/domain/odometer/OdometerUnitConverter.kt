package com.example.gearkeeper.domain.odometer

import com.example.gearkeeper.domain.preferences.DistanceUnit
import java.util.Locale
import kotlin.math.roundToInt

object OdometerUnitConverter {
    private const val KM_PER_MILE: Double = 1.609344

    fun userInputToStoredKm(raw: Int, unit: DistanceUnit): Int {
        return when (unit) {
            DistanceUnit.METRIC_KM -> raw
            DistanceUnit.IMPERIAL_MILES -> (raw * KM_PER_MILE).roundToInt()
        }
    }

    fun formatStoredKm(km: Int, unit: DistanceUnit): String {
        return when (unit) {
            DistanceUnit.METRIC_KM -> "$km km"
            DistanceUnit.IMPERIAL_MILES -> {
                val miles: Double = km / KM_PER_MILE
                String.format(Locale.US, "%.1f mi", miles)
            }
        }
    }

    fun storedKmToDisplayIntString(km: Int, unit: DistanceUnit): String {
        return when (unit) {
            DistanceUnit.METRIC_KM -> km.toString()
            DistanceUnit.IMPERIAL_MILES -> (km / KM_PER_MILE).roundToInt().toString()
        }
    }
}
