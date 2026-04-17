package com.example.gearkeeper.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

data class MaintenanceSummary(
    val id: Long,
    val title: String,
    val performedDate: String,
    val odometerKm: Int?,
    val serviceNamesSummary: String,
)

/**
 * Latest logged performance of a catalog service on a vehicle (for incoming / next-service list).
 */
class MaintenanceRepository(
    context: android.content.Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    /**
     * @param searchQuery optional; filters grouped rows by title, date, odometer, and concatenated service names.
     * @param preferSpanishServiceNames when true, concatenated catalog names use [service_type.name_es] when set.
     */
    fun getMaintenancesForVehicle(
        vehicleId: Long,
        searchQuery: String? = null,
        preferSpanishServiceNames: Boolean = false,
    ): List<MaintenanceSummary> {
        val needle: String? = SqlSearch.normalizeNeedle(raw = searchQuery)
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val result: MutableList<MaintenanceSummary> = mutableListOf()
        val localeTag: String = if (preferSpanishServiceNames) "es" else "en"
        val inner: String = """
            SELECT m.id, m.title, m.performed_date, m.odometer_km,
                   GROUP_CONCAT(
                       CASE WHEN ? = 'es' AND s.name_es IS NOT NULL AND LENGTH(TRIM(s.name_es)) > 0
                            THEN s.name_es ELSE s.name END,
                       ', '
                   ) AS services
            FROM maintenance m
            LEFT JOIN maintenance_service ms ON ms.maintenance_id = m.id
            LEFT JOIN service_type s ON s.id = ms.service_type_id
            WHERE m.vehicle_id = ?
            GROUP BY m.id
        """.trimIndent()
        val sql: String
        val bindArgs: Array<String>
        if (needle == null) {
            sql = "$inner ORDER BY m.performed_date DESC, m.id DESC"
            bindArgs = arrayOf(localeTag, vehicleId.toString())
        } else {
            sql = """
                SELECT visit.id, visit.title, visit.performed_date, visit.odometer_km, visit.services
                FROM (
                    $inner
                ) AS visit
                WHERE INSTR(
                    LOWER(
                        COALESCE(visit.title, '') || ' ' ||
                        COALESCE(visit.performed_date, '') || ' ' ||
                        COALESCE(CAST(visit.odometer_km AS TEXT), '') || ' ' ||
                        COALESCE(visit.services, '')
                    ),
                    ?
                ) > 0
                ORDER BY visit.performed_date DESC, visit.id DESC
            """.trimIndent()
            bindArgs = arrayOf(localeTag, vehicleId.toString(), needle)
        }
        val cursor = db.rawQuery(sql, bindArgs)
        cursor.use { c ->
            while (c.moveToNext()) {
                val odometer: Int? = if (c.isNull(3)) {
                    null
                } else {
                    c.getInt(3)
                }
                result.add(
                    MaintenanceSummary(
                        id = c.getLong(0),
                        title = c.getString(1),
                        performedDate = c.getString(2),
                        odometerKm = odometer,
                        serviceNamesSummary = c.getString(4).orEmpty(),
                    )
                )
            }
        }
        return result
    }

    /**
     * @param odometerKm required for new visits (stored on [maintenance] and optionally on [odometer_reading]).
     * @param addOdometerReading when true, inserts a row into [odometer_reading] in the same transaction.
     */
    fun insertMaintenance(
        vehicleId: Long,
        title: String,
        performedDate: String,
        odometerKm: Int,
        notes: String?,
        serviceTypeIds: List<Long>,
        addOdometerReading: Boolean,
        replacedParts: List<ReplacedPartInput> = emptyList(),
    ): Long? {
        if (serviceTypeIds.isEmpty()) {
            return null
        }
        val db: SQLiteDatabase = dbHelper().writableDatabase
        db.beginTransaction()
        return try {
            val maintenanceValues = ContentValues().apply {
                put("vehicle_id", vehicleId)
                put("title", title.trim())
                put("performed_date", performedDate.trim())
                put("odometer_km", odometerKm)
                if (notes.isNullOrBlank()) {
                    putNull("notes")
                } else {
                    put("notes", notes.trim())
                }
            }
            val maintenanceId: Long = db.insert("maintenance", null, maintenanceValues)
            if (maintenanceId == -1L) {
                db.endTransaction()
                return null
            }
            for (serviceTypeId: Long in serviceTypeIds.toSet()) {
                val line = ContentValues().apply {
                    put("maintenance_id", maintenanceId)
                    put("service_type_id", serviceTypeId)
                }
                val lineId: Long = db.insert("maintenance_service", null, line)
                if (lineId == -1L) {
                    db.endTransaction()
                    return null
                }
            }
            if (addOdometerReading) {
                OdometerRepository.insertReadingRow(
                    db = db,
                    vehicleId = vehicleId,
                    odometerKm = odometerKm,
                    recordedAtTrimmed = performedDate.trim(),
                    maintenanceId = maintenanceId,
                )
            }
            for (part: ReplacedPartInput in replacedParts) {
                val partTitle: String = part.title.trim()
                if (partTitle.isEmpty()) {
                    continue
                }
                val partValues = ContentValues().apply {
                    put("maintenance_id", maintenanceId)
                    put("title", partTitle)
                    put("part_or_serial", part.partOrSerial.trim())
                    if (part.brand.isNullOrBlank()) {
                        putNull("brand")
                    } else {
                        put("brand", part.brand.trim())
                    }
                    if (part.store.isNullOrBlank()) {
                        putNull("store")
                    } else {
                        put("store", part.store.trim())
                    }
                    if (part.notes.isNullOrBlank()) {
                        putNull("notes")
                    } else {
                        put("notes", part.notes.trim())
                    }
                }
                val partRowId: Long = db.insert("maintenance_replaced_part", null, partValues)
                if (partRowId == -1L) {
                    db.endTransaction()
                    return null
                }
            }
            db.setTransactionSuccessful()
            maintenanceId
        } catch (error: Exception) {
            Log.e("MaintenanceRepository", "insertMaintenance", error)
            null
        } finally {
            db.endTransaction()
        }
    }

    fun getMaintenanceEditLoad(maintenanceId: Long): MaintenanceEditLoad? {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT vehicle_id, title, performed_date, odometer_km, notes
            FROM maintenance
            WHERE id = ?
            """.trimIndent(),
            arrayOf(maintenanceId.toString()),
        )
        val base: MaintenanceEditLoad = cursor.use { c ->
            if (!c.moveToFirst()) {
                return@use null
            }
            val odo: Int? = if (c.isNull(3)) null else c.getInt(3)
            MaintenanceEditLoad(
                vehicleId = c.getLong(0),
                title = c.getString(1).orEmpty(),
                performedDate = c.getString(2).orEmpty(),
                odometerKm = odo,
                notes = if (c.isNull(4)) null else c.getString(4),
                serviceTypeIds = emptyList(),
                replacedParts = emptyList(),
            )
        } ?: return null
        val serviceIds: MutableList<Long> = mutableListOf()
        db.rawQuery(
            "SELECT service_type_id FROM maintenance_service WHERE maintenance_id = ? ORDER BY id",
            arrayOf(maintenanceId.toString()),
        ).use { sc ->
            while (sc.moveToNext()) {
                serviceIds.add(sc.getLong(0))
            }
        }
        val parts: MutableList<ReplacedPartInput> = mutableListOf()
        db.rawQuery(
            """
            SELECT title, part_or_serial, brand, store, notes
            FROM maintenance_replaced_part
            WHERE maintenance_id = ?
            ORDER BY id
            """.trimIndent(),
            arrayOf(maintenanceId.toString()),
        ).use { pc ->
            while (pc.moveToNext()) {
                parts.add(
                    ReplacedPartInput(
                        title = pc.getString(0).orEmpty(),
                        partOrSerial = pc.getString(1).orEmpty(),
                        brand = if (pc.isNull(2)) null else pc.getString(2),
                        store = if (pc.isNull(3)) null else pc.getString(3),
                        notes = if (pc.isNull(4)) null else pc.getString(4),
                    ),
                )
            }
        }
        return base.copy(
            serviceTypeIds = serviceIds,
            replacedParts = parts,
        )
    }

    /**
     * Updates maintenance row, service lines, replaced parts, and optionally the linked odometer reading.
     */
    fun updateMaintenance(
        maintenanceId: Long,
        vehicleId: Long,
        title: String,
        performedDate: String,
        odometerKm: Int,
        notes: String?,
        serviceTypeIds: List<Long>,
        addOdometerReading: Boolean,
        replacedParts: List<ReplacedPartInput>,
    ): Boolean {
        if (serviceTypeIds.isEmpty()) {
            return false
        }
        val db: SQLiteDatabase = dbHelper().writableDatabase
        db.beginTransaction()
        return try {
            val maintenanceValues = ContentValues().apply {
                put("vehicle_id", vehicleId)
                put("title", title.trim())
                put("performed_date", performedDate.trim())
                put("odometer_km", odometerKm)
                if (notes.isNullOrBlank()) {
                    putNull("notes")
                } else {
                    put("notes", notes.trim())
                }
            }
            val updated: Int = db.update(
                "maintenance",
                maintenanceValues,
                "id = ?",
                arrayOf(maintenanceId.toString()),
            )
            if (updated != 1) {
                db.endTransaction()
                return false
            }
            db.delete("maintenance_service", "maintenance_id = ?", arrayOf(maintenanceId.toString()))
            for (serviceTypeId: Long in serviceTypeIds.toSet()) {
                val line = ContentValues().apply {
                    put("maintenance_id", maintenanceId)
                    put("service_type_id", serviceTypeId)
                }
                if (db.insert("maintenance_service", null, line) == -1L) {
                    db.endTransaction()
                    return false
                }
            }
            db.delete("maintenance_replaced_part", "maintenance_id = ?", arrayOf(maintenanceId.toString()))
            for (part: ReplacedPartInput in replacedParts) {
                val partTitle: String = part.title.trim()
                if (partTitle.isEmpty()) {
                    continue
                }
                val partValues = ContentValues().apply {
                    put("maintenance_id", maintenanceId)
                    put("title", partTitle)
                    put("part_or_serial", part.partOrSerial.trim())
                    if (part.brand.isNullOrBlank()) {
                        putNull("brand")
                    } else {
                        put("brand", part.brand.trim())
                    }
                    if (part.store.isNullOrBlank()) {
                        putNull("store")
                    } else {
                        put("store", part.store.trim())
                    }
                    if (part.notes.isNullOrBlank()) {
                        putNull("notes")
                    } else {
                        put("notes", part.notes.trim())
                    }
                }
                if (db.insert("maintenance_replaced_part", null, partValues) == -1L) {
                    db.endTransaction()
                    return false
                }
            }
            if (addOdometerReading) {
                val readingCursor = db.rawQuery(
                    "SELECT id FROM odometer_reading WHERE maintenance_id = ? LIMIT 1",
                    arrayOf(maintenanceId.toString()),
                )
                val existingId: Long? = readingCursor.use { rc ->
                    if (rc.moveToFirst()) rc.getLong(0) else null
                }
                if (existingId != null) {
                    val readingValues = ContentValues().apply {
                        put("odometer_km", odometerKm)
                        put("recorded_at", performedDate.trim())
                        put("vehicle_id", vehicleId)
                    }
                    db.update(
                        "odometer_reading",
                        readingValues,
                        "id = ?",
                        arrayOf(existingId.toString()),
                    )
                } else {
                    OdometerRepository.insertReadingRow(
                        db = db,
                        vehicleId = vehicleId,
                        odometerKm = odometerKm,
                        recordedAtTrimmed = performedDate.trim(),
                        maintenanceId = maintenanceId,
                    )
                }
            } else {
                db.delete(
                    "odometer_reading",
                    "maintenance_id = ?",
                    arrayOf(maintenanceId.toString()),
                )
            }
            db.setTransactionSuccessful()
            true
        } catch (error: Exception) {
            Log.e("MaintenanceRepository", "updateMaintenance", error)
            false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteMaintenance(
        maintenanceId: Long,
        deleteLinkedOdometerReadings: Boolean,
    ): Boolean {
        val db: SQLiteDatabase = dbHelper().writableDatabase
        db.beginTransaction()
        return try {
            if (deleteLinkedOdometerReadings) {
                db.delete(
                    "odometer_reading",
                    "maintenance_id = ?",
                    arrayOf(maintenanceId.toString()),
                )
            } else {
                val clearLink = ContentValues().apply {
                    putNull("maintenance_id")
                }
                db.update(
                    "odometer_reading",
                    clearLink,
                    "maintenance_id = ?",
                    arrayOf(maintenanceId.toString()),
                )
            }
            db.delete("maintenance_replaced_part", "maintenance_id = ?", arrayOf(maintenanceId.toString()))
            db.delete("maintenance_service", "maintenance_id = ?", arrayOf(maintenanceId.toString()))
            val deleted: Int = db.delete("maintenance", "id = ?", arrayOf(maintenanceId.toString()))
            if (deleted != 1) {
                db.endTransaction()
                return false
            }
            db.setTransactionSuccessful()
            true
        } catch (error: Exception) {
            Log.e("MaintenanceRepository", "deleteMaintenance", error)
            false
        } finally {
            db.endTransaction()
        }
    }

    fun hasOdometerReadingLinked(maintenanceId: Long): Boolean {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        db.rawQuery(
            "SELECT 1 FROM odometer_reading WHERE maintenance_id = ? LIMIT 1",
            arrayOf(maintenanceId.toString()),
        ).use { c ->
            return c.moveToFirst()
        }
    }
}
