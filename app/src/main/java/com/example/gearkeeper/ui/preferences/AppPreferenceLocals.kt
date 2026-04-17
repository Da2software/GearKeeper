package com.example.gearkeeper.ui.preferences

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.gearkeeper.domain.preferences.DistanceUnit

val LocalDistanceUnit = staticCompositionLocalOf { DistanceUnit.METRIC_KM }
