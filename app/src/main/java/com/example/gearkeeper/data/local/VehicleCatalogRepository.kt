package com.example.gearkeeper.data.local

import android.content.Context

data class BrandOption(
    val id: Int,
    val name: String,
)

data class ModelOption(
    val id: Int,
    val name: String,
)

class VehicleCatalogRepository(
    context: Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    fun getBrands(vehicleType: String): List<BrandOption> {
        val db = dbHelper().readableDatabase
        val result: MutableList<BrandOption> = mutableListOf()
        val cursor = db.rawQuery(
            "SELECT id, name FROM brand WHERE vehicle_type = ? ORDER BY name ASC",
            arrayOf(vehicleType),
        )
        cursor.use { safeCursor ->
            while (safeCursor.moveToNext()) {
                result.add(
                    BrandOption(
                        id = safeCursor.getInt(0),
                        name = safeCursor.getString(1),
                    )
                )
            }
        }
        return result
    }

    fun getModels(brandId: Int): List<ModelOption> {
        val db = dbHelper().readableDatabase
        val result: MutableList<ModelOption> = mutableListOf()
        val cursor = db.rawQuery(
            "SELECT id, name FROM model WHERE brand_id = ? ORDER BY name ASC",
            arrayOf(brandId.toString()),
        )
        cursor.use { safeCursor ->
            while (safeCursor.moveToNext()) {
                result.add(
                    ModelOption(
                        id = safeCursor.getInt(0),
                        name = safeCursor.getString(1),
                    )
                )
            }
        }
        return result
    }
}

