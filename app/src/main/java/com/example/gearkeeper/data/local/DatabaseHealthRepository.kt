package com.example.gearkeeper.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase

data class DatabaseHealthResult(
    val isOk: Boolean,
    val databaseVersion: Int,
    val brandCarCount: Int,
    val brandMotorcycleCount: Int,
    val modelCount: Int,
    val vehicleCount: Int,
    val errorMessage: String?,
)

class DatabaseHealthRepository(
    context: Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    fun getHealth(): DatabaseHealthResult {
        return try {
            val db = dbHelper().readableDatabase
            val dbVersion: Int = db.version

            val brandCarCount: Int = countQuery(
                db = db,
                sql = "SELECT COUNT(*) FROM brand WHERE vehicle_type = 'car'",
            )
            val brandMotorcycleCount: Int = countQuery(
                db = db,
                sql = "SELECT COUNT(*) FROM brand WHERE vehicle_type = 'motorcycle'",
            )
            val modelCount: Int = countQuery(
                db = db,
                sql = "SELECT COUNT(*) FROM model",
            )
            val vehicleCount: Int = countQuery(
                db = db,
                sql = "SELECT COUNT(*) FROM vehicle",
            )

            DatabaseHealthResult(
                isOk = true,
                databaseVersion = dbVersion,
                brandCarCount = brandCarCount,
                brandMotorcycleCount = brandMotorcycleCount,
                modelCount = modelCount,
                vehicleCount = vehicleCount,
                errorMessage = null,
            )
        } catch (error: Exception) {
            DatabaseHealthResult(
                isOk = false,
                databaseVersion = 0,
                brandCarCount = 0,
                brandMotorcycleCount = 0,
                modelCount = 0,
                vehicleCount = 0,
                errorMessage = error.message ?: error.javaClass.simpleName,
            )
        }
    }

    private fun countQuery(
        db: SQLiteDatabase,
        sql: String,
    ): Int {
        val cursor = db.rawQuery(sql, null)
        return cursor.use { safeCursor ->
            if (safeCursor.moveToFirst()) {
                safeCursor.getInt(0)
            } else {
                0
            }
        }
    }
}
