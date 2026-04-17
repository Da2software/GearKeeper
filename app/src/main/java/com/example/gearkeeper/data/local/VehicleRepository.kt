package com.example.gearkeeper.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.gearkeeper.reminders.OdometerReminderScheduler
import java.util.Calendar

data class VehicleListItem(
    val id: Long,
    val name: String,
    val vehicleType: String,
    val brandName: String,
    val modelName: String,
    /** Latest odometer from [odometer_reading], else [vehicle.current_odometer_km]. */
    val lastOdometerKm: Int?,
)

data class VehicleEditLoad(
    val vehicleType: String,
    val brandId: Int,
    val brandName: String,
    val modelId: Int,
    val modelName: String,
    val name: String,
    val year: Int?,
    val licensePlate: String?,
    val binNumber: String?,
    val vin: String?,
    val doors: Int?,
    val currentOdometerKm: Int?,
    val odometerReminderEnabled: Boolean,
    val odometerReminderIntervalUnit: String,
    val odometerReminderIntervalValue: Int,
    val fuelType: String?,
    val transmissionType: String?,
    val transmissionSubtype: String?,
    val color: String?,
    val notes: String?,
)

class VehicleRepository(
    context: android.content.Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    /**
     * @param searchQuery optional; matches name, brand, model, and vehicle type (substring, case-insensitive).
     */
    fun getAllVehicles(searchQuery: String? = null): List<VehicleListItem> {
        val needle: String? = SqlSearch.normalizeNeedle(raw = searchQuery)
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val result: MutableList<VehicleListItem> = mutableListOf()
        val baseFrom: String = """
            FROM vehicle v
            INNER JOIN brand b ON v.brand_id = b.id
            INNER JOIN model m ON v.model_id = m.id
            LEFT JOIN odometer_reading ord ON ord.vehicle_id = v.id AND ord.id = (
                SELECT r2.id FROM odometer_reading r2
                WHERE r2.vehicle_id = v.id
                ORDER BY r2.recorded_at DESC, r2.id DESC
                LIMIT 1
            )
        """.trimIndent()
        val selectList: String = """
            SELECT v.id, v.name, v.vehicle_type, b.name, m.name,
                   COALESCE(ord.odometer_km, v.current_odometer_km) AS last_odometer
        """.trimIndent()
        val sql: String
        val bindArgs: Array<String>?
        if (needle == null) {
            sql = "$selectList $baseFrom ORDER BY v.id DESC"
            bindArgs = null
        } else {
            sql = """
                $selectList $baseFrom
                WHERE INSTR(
                    LOWER(v.name || ' ' || b.name || ' ' || m.name || ' ' || v.vehicle_type),
                    ?
                ) > 0
                ORDER BY v.id DESC
            """.trimIndent()
            bindArgs = arrayOf(needle)
        }
        val cursor = db.rawQuery(sql, bindArgs)
        cursor.use { safeCursor ->
            while (safeCursor.moveToNext()) {
                result.add(mapRowToVehicleListItem(safeCursor))
            }
        }
        return result
    }

    fun getVehicleListItem(vehicleId: Long): VehicleListItem? {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val sql: String = """
            SELECT v.id, v.name, v.vehicle_type, b.name, m.name,
                   COALESCE(ord.odometer_km, v.current_odometer_km) AS last_odometer
            FROM vehicle v
            INNER JOIN brand b ON v.brand_id = b.id
            INNER JOIN model m ON v.model_id = m.id
            LEFT JOIN odometer_reading ord ON ord.vehicle_id = v.id AND ord.id = (
                SELECT r2.id FROM odometer_reading r2
                WHERE r2.vehicle_id = v.id
                ORDER BY r2.recorded_at DESC, r2.id DESC
                LIMIT 1
            )
            WHERE v.id = ?
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(vehicleId.toString()))
        cursor.use { safeCursor ->
            if (!safeCursor.moveToFirst()) {
                return null
            }
            return mapRowToVehicleListItem(safeCursor)
        }
    }

    fun getVehicleEditLoad(vehicleId: Long): VehicleEditLoad? {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val sql: String = """
            SELECT
                v.vehicle_type,
                v.brand_id,
                b.name,
                v.model_id,
                m.name,
                v.name,
                v.year,
                v.license_plate,
                v.bin_number,
                v.vin,
                v.doors,
                COALESCE(ord.odometer_km, v.current_odometer_km) AS current_odo,
                v.odometer_reminder_enabled,
                v.odometer_reminder_interval_unit,
                v.odometer_reminder_interval_value,
                v.fuel_type,
                v.transmission_type,
                v.transmission_subtype,
                v.color,
                v.notes
            FROM vehicle v
            INNER JOIN brand b ON b.id = v.brand_id
            INNER JOIN model m ON m.id = v.model_id
            LEFT JOIN odometer_reading ord ON ord.vehicle_id = v.id AND ord.id = (
                SELECT r2.id FROM odometer_reading r2
                WHERE r2.vehicle_id = v.id
                ORDER BY r2.recorded_at DESC, r2.id DESC
                LIMIT 1
            )
            WHERE v.id = ?
            LIMIT 1
        """.trimIndent()
        db.rawQuery(sql, arrayOf(vehicleId.toString())).use { cursor ->
            if (!cursor.moveToFirst()) {
                return null
            }
            return VehicleEditLoad(
                vehicleType = cursor.getString(0).orEmpty(),
                brandId = cursor.getInt(1),
                brandName = cursor.getString(2).orEmpty(),
                modelId = cursor.getInt(3),
                modelName = cursor.getString(4).orEmpty(),
                name = cursor.getString(5).orEmpty(),
                year = if (cursor.isNull(6)) null else cursor.getInt(6),
                licensePlate = if (cursor.isNull(7)) null else cursor.getString(7),
                binNumber = if (cursor.isNull(8)) null else cursor.getString(8),
                vin = if (cursor.isNull(9)) null else cursor.getString(9),
                doors = if (cursor.isNull(10)) null else cursor.getInt(10),
                currentOdometerKm = if (cursor.isNull(11)) null else cursor.getInt(11),
                odometerReminderEnabled = cursor.getInt(12) == 1,
                odometerReminderIntervalUnit = cursor.getString(13).orEmpty(),
                odometerReminderIntervalValue = cursor.getInt(14).coerceAtLeast(1),
                fuelType = if (cursor.isNull(15)) null else cursor.getString(15),
                transmissionType = if (cursor.isNull(16)) null else cursor.getString(16),
                transmissionSubtype = if (cursor.isNull(17)) null else cursor.getString(17),
                color = if (cursor.isNull(18)) null else cursor.getString(18),
                notes = if (cursor.isNull(19)) null else cursor.getString(19),
            )
        }
    }

    /**
     * @return `null` on success, or a short user-visible error message.
     */
    fun insertVehicle(input: NewVehicleInput): String? {
        val values = ContentValues().apply {
            put("vehicle_type", input.vehicleType)
            put("brand_id", input.brandId)
            put("model_id", input.modelId)
            put("name", input.name.trim())
            if (input.year != null) {
                put("year", input.year)
            } else {
                putNull("year")
            }
            putNullableStringOrNull("license_plate", input.licensePlate)
            putNullableStringOrNull("bin_number", input.binNumber)
            putNullableStringOrNull("vin", input.vin)
            if (input.doors != null) {
                put("doors", input.doors)
            } else {
                putNull("doors")
            }
            putNullableStringOrNull("fuel_type", input.fuelType)
            putNullableStringOrNull("transmission_type", input.transmissionType)
            putNullableStringOrNull("transmission_subtype", input.transmissionSubtype)
            if (input.currentOdometerKm != null) {
                put("current_odometer_km", input.currentOdometerKm)
            } else {
                putNull("current_odometer_km")
            }
            put("odometer_reminder_enabled", if (input.odometerReminderEnabled) 1 else 0)
            put("odometer_reminder_interval_unit", input.odometerReminderIntervalUnit)
            put("odometer_reminder_interval_value", input.odometerReminderIntervalValue)
            putNull("odometer_reminder_skip_until")
            putNullableStringOrNull("color", input.color)
            putNullableStringOrNull("notes", input.notes)
        }

        return try {
            val db: SQLiteDatabase = dbHelper().writableDatabase
            db.beginTransaction()
            try {
                val rowId: Long = db.insert("vehicle", null, values)
                if (rowId == -1L) {
                    return "Could not save vehicle"
                }
                val kmLong: Long? = input.currentOdometerKm
                if (kmLong != null) {
                    val kmInt: Int = kmLong.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
                    OdometerRepository.insertReadingRow(
                        db = db,
                        vehicleId = rowId,
                        odometerKm = kmInt,
                        recordedAtTrimmed = todayIsoDate(),
                        maintenanceId = null,
                    )
                }
                db.setTransactionSuccessful()
                OdometerReminderScheduler.scheduleImmediateCheck(context = appContext)
                null
            } finally {
                db.endTransaction()
            }
        } catch (error: SQLiteConstraintException) {
            Log.w("VehicleRepository", "insertVehicle constraint", error)
            if (error.message?.contains("vin", ignoreCase = true) == true) {
                "This VIN is already registered"
            } else {
                "Could not save vehicle (invalid data)"
            }
        } catch (error: Exception) {
            Log.e("VehicleRepository", "insertVehicle", error)
            "Could not save vehicle"
        }
    }

    fun deleteVehicleById(vehicleId: Long): Boolean {
        return try {
            val db: SQLiteDatabase = dbHelper().writableDatabase
            val deleted: Int = db.delete(
                "vehicle",
                "id = ?",
                arrayOf(vehicleId.toString()),
            )
            deleted == 1
        } catch (error: Exception) {
            Log.e("VehicleRepository", "deleteVehicleById", error)
            false
        }
    }

    /**
     * @return `null` on success, or a short user-visible error message.
     */
    fun updateVehicle(vehicleId: Long, input: NewVehicleInput): String? {
        val values = ContentValues().apply {
            put("vehicle_type", input.vehicleType)
            put("brand_id", input.brandId)
            put("model_id", input.modelId)
            put("name", input.name.trim())
            if (input.year != null) {
                put("year", input.year)
            } else {
                putNull("year")
            }
            putNullableStringOrNull("license_plate", input.licensePlate)
            putNullableStringOrNull("bin_number", input.binNumber)
            putNullableStringOrNull("vin", input.vin)
            if (input.doors != null) {
                put("doors", input.doors)
            } else {
                putNull("doors")
            }
            if (input.currentOdometerKm != null) {
                put("current_odometer_km", input.currentOdometerKm)
            } else {
                putNull("current_odometer_km")
            }
            put("odometer_reminder_enabled", if (input.odometerReminderEnabled) 1 else 0)
            put("odometer_reminder_interval_unit", input.odometerReminderIntervalUnit)
            put("odometer_reminder_interval_value", input.odometerReminderIntervalValue)
            putNullableStringOrNull("fuel_type", input.fuelType)
            putNullableStringOrNull("transmission_type", input.transmissionType)
            putNullableStringOrNull("transmission_subtype", input.transmissionSubtype)
            putNullableStringOrNull("color", input.color)
            putNullableStringOrNull("notes", input.notes)
            put("updated_at", todaySqlTimestamp())
        }
        return try {
            val db: SQLiteDatabase = dbHelper().writableDatabase
            val updated: Int = db.update(
                "vehicle",
                values,
                "id = ?",
                arrayOf(vehicleId.toString()),
            )
            if (updated != 1) {
                "Could not save vehicle"
            } else {
                OdometerReminderScheduler.scheduleImmediateCheck(context = appContext)
                null
            }
        } catch (error: SQLiteConstraintException) {
            Log.w("VehicleRepository", "updateVehicle constraint", error)
            if (error.message?.contains("vin", ignoreCase = true) == true) {
                "This VIN is already registered"
            } else {
                "Could not save vehicle (invalid data)"
            }
        } catch (error: Exception) {
            Log.e("VehicleRepository", "updateVehicle", error)
            "Could not save vehicle"
        }
    }

    private fun mapRowToVehicleListItem(cursor: android.database.Cursor): VehicleListItem {
        val lastOdometer: Int? = if (cursor.isNull(5)) {
            null
        } else {
            cursor.getInt(5)
        }
        return VehicleListItem(
            id = cursor.getLong(0),
            name = cursor.getString(1),
            vehicleType = cursor.getString(2),
            brandName = cursor.getString(3),
            modelName = cursor.getString(4),
            lastOdometerKm = lastOdometer,
        )
    }

    private fun ContentValues.putNullableStringOrNull(
        key: String,
        value: String?,
    ): Unit {
        val trimmed: String? = value?.trim()?.takeIf { it.isNotEmpty() }
        if (trimmed == null) {
            putNull(key)
        } else {
            put(key, trimmed)
        }
    }

    private fun todayIsoDate(): String {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    private fun todaySqlTimestamp(): String {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        val second: Int = calendar.get(Calendar.SECOND)
        return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second)
    }
}
