package com.example.gearkeeper.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class OdometerReminderVehicleStatus {
    Pending,
    Skipped,
    UpToDate,
}

data class OdometerReminderPendingVehicle(
    val vehicleId: Long,
    val vehicleName: String,
    val vehicleBrandModelLine: String,
    val latestReadingCreatedAt: String?,
    val reminderIntervalUnit: String,
    val reminderIntervalValue: Int,
)

data class OdometerReminderVehicleStatusRow(
    val vehicleId: Long,
    val vehicleName: String,
    val vehicleBrandModelLine: String,
    val latestReadingCreatedAt: String?,
    val reminderIntervalUnit: String,
    val reminderIntervalValue: Int,
    val skipUntil: String?,
    val nextDueAt: String,
    val status: OdometerReminderVehicleStatus,
)

class OdometerReminderRepository(
    context: android.content.Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    fun getPendingVehicles(now: LocalDateTime = LocalDateTime.now()): List<OdometerReminderPendingVehicle> {
        return getVehicleReminderStatuses(now = now)
            .filter { row: OdometerReminderVehicleStatusRow -> row.status == OdometerReminderVehicleStatus.Pending }
            .map { row: OdometerReminderVehicleStatusRow ->
                OdometerReminderPendingVehicle(
                    vehicleId = row.vehicleId,
                    vehicleName = row.vehicleName,
                    vehicleBrandModelLine = row.vehicleBrandModelLine,
                    latestReadingCreatedAt = row.latestReadingCreatedAt,
                    reminderIntervalUnit = row.reminderIntervalUnit,
                    reminderIntervalValue = row.reminderIntervalValue,
                )
            }
    }

    fun getVehicleReminderStatuses(now: LocalDateTime = LocalDateTime.now()): List<OdometerReminderVehicleStatusRow> {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val sql: String = """
            SELECT
                v.id,
                v.name,
                b.name,
                m.name,
                v.created_at,
                v.odometer_reminder_interval_unit,
                v.odometer_reminder_interval_value,
                v.odometer_reminder_skip_until,
                (
                    SELECT r.created_at
                    FROM odometer_reading r
                    WHERE r.vehicle_id = v.id
                    ORDER BY r.created_at DESC, r.id DESC
                    LIMIT 1
                ) AS latest_reading_created_at
            FROM vehicle v
            INNER JOIN brand b ON b.id = v.brand_id
            INNER JOIN model m ON m.id = v.model_id
            WHERE v.odometer_reminder_enabled = 1
            ORDER BY v.name COLLATE NOCASE ASC, v.id ASC
        """.trimIndent()
        val rows: MutableList<OdometerReminderVehicleStatusRow> = mutableListOf()
        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                val vehicleId: Long = cursor.getLong(0)
                val vehicleName: String = cursor.getString(1).orEmpty()
                val brandName: String = cursor.getString(2).orEmpty()
                val modelName: String = cursor.getString(3).orEmpty()
                val vehicleCreatedAt: String = cursor.getString(4).orEmpty()
                val intervalUnit: String = cursor.getString(5).orEmpty()
                val intervalValue: Int = cursor.getInt(6).coerceAtLeast(1)
                val skipUntilRaw: String? = if (cursor.isNull(7)) null else cursor.getString(7)
                val latestReadingCreatedAt: String? = if (cursor.isNull(8)) null else cursor.getString(8)
                val skippedUntil: LocalDateTime? = parseSqlDateTime(raw = skipUntilRaw)
                val baselineCreatedAt: LocalDateTime = parseSqlDateTime(raw = latestReadingCreatedAt)
                    ?: parseSqlDateTime(raw = vehicleCreatedAt)
                    ?: now
                val nextDueAt: LocalDateTime = when (intervalUnit) {
                    "MONTHS" -> baselineCreatedAt.plusMonths(intervalValue.toLong())
                    else -> baselineCreatedAt.plusDays(intervalValue.toLong())
                }
                val status: OdometerReminderVehicleStatus = when {
                    skippedUntil != null && now.isBefore(skippedUntil) -> OdometerReminderVehicleStatus.Skipped
                    !now.isBefore(nextDueAt) -> OdometerReminderVehicleStatus.Pending
                    else -> OdometerReminderVehicleStatus.UpToDate
                }
                rows.add(
                    OdometerReminderVehicleStatusRow(
                        vehicleId = vehicleId,
                        vehicleName = vehicleName,
                        vehicleBrandModelLine = listOf(brandName, modelName)
                            .filter { part: String -> part.isNotBlank() }
                            .joinToString(separator = " "),
                        latestReadingCreatedAt = latestReadingCreatedAt,
                        reminderIntervalUnit = intervalUnit,
                        reminderIntervalValue = intervalValue,
                        skipUntil = skipUntilRaw,
                        nextDueAt = formatSqlDateTime(value = nextDueAt),
                        status = status,
                    ),
                )
            }
        }
        return rows
    }

    fun skipVehicleReminder(vehicleId: Long, now: LocalDateTime = LocalDateTime.now()): Boolean {
        val db: SQLiteDatabase = dbHelper().writableDatabase
        val settings: Pair<String, Int>? = db.rawQuery(
            """
            SELECT odometer_reminder_interval_unit, odometer_reminder_interval_value
            FROM vehicle
            WHERE id = ?
            """.trimIndent(),
            arrayOf(vehicleId.toString()),
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                Pair(cursor.getString(0).orEmpty(), cursor.getInt(1).coerceAtLeast(1))
            }
        }
        val resolved: Pair<String, Int> = settings ?: return false
        val skipUntil: LocalDateTime = when (resolved.first) {
            "MONTHS" -> now.plusMonths(resolved.second.toLong())
            else -> now.plusDays(resolved.second.toLong())
        }
        val values: ContentValues = ContentValues().apply {
            put("odometer_reminder_skip_until", formatSqlDateTime(value = skipUntil))
            put("updated_at", formatSqlDateTime(value = now))
        }
        val updated: Int = db.update("vehicle", values, "id = ?", arrayOf(vehicleId.toString()))
        return updated == 1
    }

    fun clearReminderSkip(vehicleId: Long): Boolean {
        val db: SQLiteDatabase = dbHelper().writableDatabase
        val values: ContentValues = ContentValues().apply {
            putNull("odometer_reminder_skip_until")
            put("updated_at", formatSqlDateTime(value = LocalDateTime.now()))
        }
        val updated: Int = db.update("vehicle", values, "id = ?", arrayOf(vehicleId.toString()))
        return updated == 1
    }

    private fun parseSqlDateTime(raw: String?): LocalDateTime? {
        if (raw == null) {
            return null
        }
        val trimmed: String = raw.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val normalized: String = trimmed.replace(' ', 'T')
        val patterns: List<DateTimeFormatter> = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        )
        for (formatter: DateTimeFormatter in patterns) {
            try {
                return if (formatter == DateTimeFormatter.ISO_LOCAL_DATE) {
                    LocalDate.parse(trimmed, formatter).atStartOfDay()
                } else {
                    LocalDateTime.parse(normalized, formatter)
                }
            } catch (_: DateTimeParseException) {
                // Try next format.
            }
        }
        return null
    }

    private fun formatSqlDateTime(value: LocalDateTime): String {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
