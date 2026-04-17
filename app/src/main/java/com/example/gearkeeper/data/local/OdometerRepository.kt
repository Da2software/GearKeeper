package com.example.gearkeeper.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.gearkeeper.reminders.OdometerReminderScheduler

data class OdometerReading(
    val id: Long,
    val vehicleId: Long,
    val odometerKm: Int,
    val recordedAt: String,
    val maintenanceId: Long?,
)

class OdometerRepository(
    context: android.content.Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    fun getReadingsForVehicle(vehicleId: Long): List<OdometerReading> {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val sql: String = """
            SELECT id, vehicle_id, odometer_km, recorded_at, maintenance_id
            FROM odometer_reading
            WHERE vehicle_id = ?
            ORDER BY recorded_at DESC, id DESC
        """.trimIndent()
        val result: MutableList<OdometerReading> = mutableListOf()
        db.rawQuery(sql, arrayOf(vehicleId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    OdometerReading(
                        id = cursor.getLong(0),
                        vehicleId = cursor.getLong(1),
                        odometerKm = cursor.getInt(2),
                        recordedAt = cursor.getString(3),
                        maintenanceId = if (cursor.isNull(4)) {
                            null
                        } else {
                            cursor.getLong(4)
                        },
                    ),
                )
            }
        }
        return result
    }

    fun insertReading(
        vehicleId: Long,
        odometerKm: Int,
        recordedAt: String,
        maintenanceId: Long?,
    ): Long? {
        val db: SQLiteDatabase = dbHelper().writableDatabase
        return try {
            val rowId: Long = insertReadingRow(
                db = db,
                vehicleId = vehicleId,
                odometerKm = odometerKm,
                recordedAtTrimmed = recordedAt.trim(),
                maintenanceId = maintenanceId,
            )
            clearVehicleReminderSkip(db = db, vehicleId = vehicleId)
            OdometerReminderScheduler.scheduleImmediateCheck(context = appContext)
            rowId
        } catch (error: Exception) {
            Log.e("OdometerRepository", "insertReading", error)
            null
        }
    }

    fun updateReading(
        readingId: Long,
        odometerKm: Int,
        recordedAt: String,
    ): Boolean {
        return try {
            val db: SQLiteDatabase = dbHelper().writableDatabase
            val values: ContentValues = ContentValues().apply {
                put("odometer_km", odometerKm)
                put("recorded_at", recordedAt.trim())
                put("updated_at", sqlNow())
            }
            val vehicleId: Long? = db.rawQuery(
                "SELECT vehicle_id FROM odometer_reading WHERE id = ? LIMIT 1",
                arrayOf(readingId.toString()),
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else null
            }
            val updated: Int = db.update(
                "odometer_reading",
                values,
                "id = ?",
                arrayOf(readingId.toString()),
            )
            if (updated == 1 && vehicleId != null) {
                clearVehicleReminderSkip(db = db, vehicleId = vehicleId)
                OdometerReminderScheduler.scheduleImmediateCheck(context = appContext)
            }
            updated == 1
        } catch (error: Exception) {
            Log.e("OdometerRepository", "updateReading", error)
            false
        }
    }

    fun getLatestOdometerKmForVehicleExcludingMaintenance(
        vehicleId: Long,
        excludeMaintenanceId: Long,
    ): Int? {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val sql: String = """
            SELECT MAX(odometer_km)
            FROM odometer_reading
            WHERE vehicle_id = ?
              AND (maintenance_id IS NULL OR maintenance_id != ?)
        """.trimIndent()
        db.rawQuery(
            sql,
            arrayOf(vehicleId.toString(), excludeMaintenanceId.toString()),
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return null
            }
            return if (cursor.isNull(0)) {
                null
            } else {
                cursor.getInt(0)
            }
        }
    }

    fun deleteReading(readingId: Long): Boolean {
        return try {
            val db: SQLiteDatabase = dbHelper().writableDatabase
            val deleted: Int = db.delete(
                "odometer_reading",
                "id = ?",
                arrayOf(readingId.toString()),
            )
            if (deleted == 1) {
                OdometerReminderScheduler.scheduleImmediateCheck(context = appContext)
            }
            deleted == 1
        } catch (error: Exception) {
            Log.e("OdometerRepository", "deleteReading", error)
            false
        }
    }

    companion object {
        /**
         * Insert a row using an existing DB handle (e.g. inside another repository transaction).
         * @throws IllegalStateException if the insert fails.
         */
        fun insertReadingRow(
            db: SQLiteDatabase,
            vehicleId: Long,
            odometerKm: Int,
            recordedAtTrimmed: String,
            maintenanceId: Long?,
        ): Long {
            val values: ContentValues = ContentValues().apply {
                put("vehicle_id", vehicleId)
                put("odometer_km", odometerKm)
                put("recorded_at", recordedAtTrimmed)
                put("updated_at", sqlNow())
                if (maintenanceId != null) {
                    put("maintenance_id", maintenanceId)
                } else {
                    putNull("maintenance_id")
                }
            }
            val rowId: Long = db.insert("odometer_reading", null, values)
            if (rowId == -1L) {
                throw IllegalStateException("odometer_reading insert failed")
            }
            return rowId
        }

        private fun clearVehicleReminderSkip(db: SQLiteDatabase, vehicleId: Long): Unit {
            val values: ContentValues = ContentValues().apply {
                putNull("odometer_reminder_skip_until")
                put("updated_at", sqlNow())
            }
            db.update("vehicle", values, "id = ?", arrayOf(vehicleId.toString()))
        }

        private fun sqlNow(): String = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
