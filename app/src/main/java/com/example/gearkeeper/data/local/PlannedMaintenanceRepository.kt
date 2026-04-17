package com.example.gearkeeper.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.gearkeeper.domain.planned.PlannedScheduleMode

data class PlannedMaintenanceIncomingRow(
    val id: Long,
    val vehicleId: Long,
    val vehicleName: String,
    val vehicleBrandName: String,
    val vehicleModelName: String,
    val title: String,
    val servicesSummary: String,
    val firstPresetServiceTypeId: Long?,
    val scheduleMode: PlannedScheduleMode,
    val targetDate: String?,
    val targetOdometerKm: Int?,
    val leadDays: Int,
    val leadOdometerKm: Int,
    val currentOdometerKm: Int?,
)

/** Row fields needed to pre-fill the planned maintenance form when editing. */
data class PlannedMaintenanceEditRow(
    val vehicleId: Long,
    val title: String,
    val scheduleMode: PlannedScheduleMode,
    val targetDate: String?,
    val targetOdometerKm: Int?,
    val leadDays: Int,
    val leadOdometerKm: Int,
    val serviceTypeIds: List<Long>,
)

class PlannedMaintenanceRepository(
    context: android.content.Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    /**
     * All planned rows with joined service names and latest vehicle odometer for status rules.
     */
    fun getAllPlannedIncomingRows(preferSpanishServiceNames: Boolean): List<PlannedMaintenanceIncomingRow> {
        val localeTag: String = if (preferSpanishServiceNames) "es" else "en"
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val sql: String = """
            SELECT
                pm.id,
                pm.vehicle_id,
                v.name,
                b.name,
                m.name,
                pm.title,
                GROUP_CONCAT(
                    CASE WHEN ? = 'es' AND s.name_es IS NOT NULL AND LENGTH(TRIM(s.name_es)) > 0
                         THEN s.name_es ELSE s.name END,
                    ', '
                ) AS services,
                (
                    SELECT MIN(pms2.service_type_id)
                    FROM planned_maintenance_service pms2
                    WHERE pms2.planned_maintenance_id = pm.id
                ) AS first_service_id,
                pm.schedule_mode,
                pm.target_date,
                pm.target_odometer_km,
                pm.lead_days,
                pm.lead_odometer_km,
                COALESCE(
                    (
                        SELECT r.odometer_km
                        FROM odometer_reading r
                        WHERE r.vehicle_id = pm.vehicle_id
                        ORDER BY r.recorded_at DESC, r.id DESC
                        LIMIT 1
                    ),
                    v.current_odometer_km
                ) AS current_odometer_km
            FROM planned_maintenance pm
            INNER JOIN vehicle v ON v.id = pm.vehicle_id
            INNER JOIN brand b ON v.brand_id = b.id
            INNER JOIN model m ON v.model_id = m.id
            LEFT JOIN planned_maintenance_service pms ON pms.planned_maintenance_id = pm.id
            LEFT JOIN service_type s ON s.id = pms.service_type_id
            GROUP BY pm.id
            ORDER BY pm.id DESC
        """.trimIndent()
        val result: MutableList<PlannedMaintenanceIncomingRow> = mutableListOf()
        val cursor = db.rawQuery(sql, arrayOf(localeTag))
        cursor.use { c ->
            while (c.moveToNext()) {
                val mode: PlannedScheduleMode = when (c.getString(8)) {
                    "ODOMETER" -> PlannedScheduleMode.ODOMETER
                    else -> PlannedScheduleMode.DATE
                }
                val targetDate: String? = if (c.isNull(9)) null else c.getString(9)
                val targetOdo: Int? = if (c.isNull(10)) null else c.getInt(10)
                val firstId: Long? = if (c.isNull(7)) null else c.getLong(7)
                val currentOdo: Int? = if (c.isNull(13)) null else c.getInt(13)
                result.add(
                    PlannedMaintenanceIncomingRow(
                        id = c.getLong(0),
                        vehicleId = c.getLong(1),
                        vehicleName = c.getString(2),
                        vehicleBrandName = c.getString(3).orEmpty(),
                        vehicleModelName = c.getString(4).orEmpty(),
                        title = c.getString(5),
                        servicesSummary = c.getString(6).orEmpty(),
                        firstPresetServiceTypeId = firstId,
                        scheduleMode = mode,
                        targetDate = targetDate,
                        targetOdometerKm = targetOdo,
                        leadDays = c.getInt(11),
                        leadOdometerKm = c.getInt(12),
                        currentOdometerKm = currentOdo,
                    ),
                )
            }
        }
        return result
    }

    fun insertPlanned(
        vehicleId: Long,
        title: String,
        scheduleMode: PlannedScheduleMode,
        targetDate: String?,
        targetOdometerKm: Int?,
        leadDays: Int,
        leadOdometerKm: Int,
        serviceTypeIds: List<Long>,
    ): Long? {
        if (serviceTypeIds.isEmpty()) {
            return null
        }
        val db: SQLiteDatabase = dbHelper().writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("vehicle_id", vehicleId)
                put("title", title.trim())
                put("schedule_mode", scheduleMode.name)
                when (scheduleMode) {
                    PlannedScheduleMode.DATE -> {
                        put("target_date", targetDate?.trim())
                        putNull("target_odometer_km")
                    }
                    PlannedScheduleMode.ODOMETER -> {
                        putNull("target_date")
                        put("target_odometer_km", targetOdometerKm)
                    }
                }
                put("lead_days", leadDays)
                put("lead_odometer_km", leadOdometerKm)
            }
            val plannedId: Long = db.insert("planned_maintenance", null, values)
            if (plannedId == -1L) {
                db.endTransaction()
                return null
            }
            for (serviceTypeId: Long in serviceTypeIds.toSet()) {
                val line = ContentValues().apply {
                    put("planned_maintenance_id", plannedId)
                    put("service_type_id", serviceTypeId)
                }
                val lineId: Long = db.insert("planned_maintenance_service", null, line)
                if (lineId == -1L) {
                    db.endTransaction()
                    return null
                }
            }
            db.setTransactionSuccessful()
            plannedId
        } catch (error: Exception) {
            Log.e("PlannedMaintenanceRepository", "insertPlanned", error)
            null
        } finally {
            db.endTransaction()
        }
    }

    fun getPlannedForEdit(plannedId: Long): PlannedMaintenanceEditRow? {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val mainSql: String = """
            SELECT vehicle_id, title, schedule_mode, target_date, target_odometer_km, lead_days, lead_odometer_km
            FROM planned_maintenance
            WHERE id = ?
        """.trimIndent()
        val mainCursor = db.rawQuery(mainSql, arrayOf(plannedId.toString()))
        val row: PlannedMaintenanceEditRow? = mainCursor.use { c ->
            if (!c.moveToFirst()) {
                return@use null
            }
            val mode: PlannedScheduleMode = when (c.getString(2)) {
                "ODOMETER" -> PlannedScheduleMode.ODOMETER
                else -> PlannedScheduleMode.DATE
            }
            val targetDate: String? = if (c.isNull(3)) null else c.getString(3)
            val targetOdo: Int? = if (c.isNull(4)) null else c.getInt(4)
            val vehicleId: Long = c.getLong(0)
            val title: String = c.getString(1)
            val leadDays: Int = c.getInt(5)
            val leadOdo: Int = c.getInt(6)
            val servicesSql: String = """
                SELECT service_type_id
                FROM planned_maintenance_service
                WHERE planned_maintenance_id = ?
                ORDER BY service_type_id ASC
            """.trimIndent()
            val serviceIds: MutableList<Long> = mutableListOf()
            db.rawQuery(servicesSql, arrayOf(plannedId.toString())).use { sc ->
                while (sc.moveToNext()) {
                    serviceIds.add(sc.getLong(0))
                }
            }
            PlannedMaintenanceEditRow(
                vehicleId = vehicleId,
                title = title,
                scheduleMode = mode,
                targetDate = targetDate,
                targetOdometerKm = targetOdo,
                leadDays = leadDays,
                leadOdometerKm = leadOdo,
                serviceTypeIds = serviceIds,
            )
        }
        return row
    }

    fun deletePlannedMaintenance(plannedId: Long): Boolean {
        val db: SQLiteDatabase = dbHelper().writableDatabase
        val deleted: Int = db.delete("planned_maintenance", "id = ?", arrayOf(plannedId.toString()))
        return deleted == 1
    }

    fun updatePlanned(
        plannedId: Long,
        vehicleId: Long,
        title: String,
        scheduleMode: PlannedScheduleMode,
        targetDate: String?,
        targetOdometerKm: Int?,
        leadDays: Int,
        leadOdometerKm: Int,
        serviceTypeIds: List<Long>,
    ): Boolean {
        if (serviceTypeIds.isEmpty()) {
            return false
        }
        val db: SQLiteDatabase = dbHelper().writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("vehicle_id", vehicleId)
                put("title", title.trim())
                put("schedule_mode", scheduleMode.name)
                when (scheduleMode) {
                    PlannedScheduleMode.DATE -> {
                        put("target_date", targetDate?.trim())
                        putNull("target_odometer_km")
                    }
                    PlannedScheduleMode.ODOMETER -> {
                        putNull("target_date")
                        put("target_odometer_km", targetOdometerKm)
                    }
                }
                put("lead_days", leadDays)
                put("lead_odometer_km", leadOdometerKm)
            }
            val updated: Int = db.update(
                "planned_maintenance",
                values,
                "id = ?",
                arrayOf(plannedId.toString()),
            )
            if (updated != 1) {
                db.endTransaction()
                return false
            }
            db.delete(
                "planned_maintenance_service",
                "planned_maintenance_id = ?",
                arrayOf(plannedId.toString()),
            )
            for (serviceTypeId: Long in serviceTypeIds.toSet()) {
                val line = ContentValues().apply {
                    put("planned_maintenance_id", plannedId)
                    put("service_type_id", serviceTypeId)
                }
                val lineId: Long = db.insert("planned_maintenance_service", null, line)
                if (lineId == -1L) {
                    db.endTransaction()
                    return false
                }
            }
            db.setTransactionSuccessful()
            true
        } catch (error: Exception) {
            Log.e("PlannedMaintenanceRepository", "updatePlanned", error)
            false
        } finally {
            db.endTransaction()
        }
    }
}
