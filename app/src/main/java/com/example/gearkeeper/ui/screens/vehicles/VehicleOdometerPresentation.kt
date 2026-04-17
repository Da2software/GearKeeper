package com.example.gearkeeper.ui.screens.vehicles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.example.gearkeeper.R
import com.example.gearkeeper.domain.odometer.OdometerUnitConverter
import com.example.gearkeeper.ui.preferences.LocalDistanceUnit

@Composable
@ReadOnlyComposable
fun vehicleOdometerSubtitle(lastOdometerKm: Int?): String {
    val unit = LocalDistanceUnit.current
    val emptyDash: String = stringResource(id = R.string.odometer_no_reading_em_dash)
    return if (lastOdometerKm != null) {
        OdometerUnitConverter.formatStoredKm(km = lastOdometerKm, unit = unit)
    } else {
        emptyDash
    }
}
