package com.example.gearkeeper.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import com.example.gearkeeper.data.local.ServiceTypeCatalogItem
import com.example.gearkeeper.data.local.ServiceTypeOption

@Composable
@ReadOnlyComposable
fun rememberPreferSpanishLocale(): Boolean {
    val language: String = LocalConfiguration.current.locales[0]?.language ?: ""
    return language == "es"
}

fun ServiceTypeCatalogItem.localizedDisplayName(preferSpanish: Boolean): String {
    if (preferSpanish) {
        val es: String? = nameEs?.trim()
        if (!es.isNullOrEmpty()) {
            return es
        }
    }
    return name
}

fun ServiceTypeOption.localizedDisplayName(preferSpanish: Boolean): String {
    if (preferSpanish) {
        val es: String? = nameEs?.trim()
        if (!es.isNullOrEmpty()) {
            return es
        }
    }
    return name
}
