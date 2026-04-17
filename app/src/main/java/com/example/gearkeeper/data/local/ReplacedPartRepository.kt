package com.example.gearkeeper.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class ReplacedPartRepository(
    context: android.content.Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    fun listReplacedParts(filter: ReplacedPartListFilter): List<ReplacedPartListRow> {
        val fullNeedle: String? = SqlSearch.normalizeNeedle(raw = filter.fullTextNeedle)
        val partNeedle: String? = SqlSearch.normalizeNeedle(raw = filter.partOrSerialNeedle)
        val brandNeedle: String? = SqlSearch.normalizeNeedle(raw = filter.brandNeedle)
        val storeNeedle: String? = SqlSearch.normalizeNeedle(raw = filter.storeNeedle)
        val notesNeedle: String? = SqlSearch.normalizeNeedle(raw = filter.notesNeedle)
        val conditions: MutableList<String> = mutableListOf()
        val args: MutableList<String> = mutableListOf()
        if (fullNeedle != null) {
            conditions.add(
                """
                INSTR(
                    LOWER(
                        COALESCE(rp.title, '') || ' ' ||
                        COALESCE(rp.part_or_serial, '') || ' ' ||
                        COALESCE(rp.brand, '') || ' ' ||
                        COALESCE(rp.store, '') || ' ' ||
                        COALESCE(rp.notes, '') || ' ' ||
                        COALESCE(m.title, '')
                    ),
                    ?
                ) > 0
                """.trimIndent().replace("\n", " "),
            )
            args.add(fullNeedle)
        }
        if (partNeedle != null) {
            conditions.add("INSTR(LOWER(COALESCE(rp.part_or_serial, '')), ?) > 0")
            args.add(partNeedle)
        }
        if (brandNeedle != null) {
            conditions.add("INSTR(LOWER(COALESCE(rp.brand, '')), ?) > 0")
            args.add(brandNeedle)
        }
        if (storeNeedle != null) {
            conditions.add("INSTR(LOWER(COALESCE(rp.store, '')), ?) > 0")
            args.add(storeNeedle)
        }
        if (notesNeedle != null) {
            conditions.add("INSTR(LOWER(COALESCE(rp.notes, '')), ?) > 0")
            args.add(notesNeedle)
        }
        if (filter.vehicleId != null) {
            conditions.add("m.vehicle_id = ?")
            args.add(filter.vehicleId.toString())
        }
        if (!filter.performedDateFrom.isNullOrBlank()) {
            conditions.add("m.performed_date >= ?")
            args.add(filter.performedDateFrom.trim())
        }
        if (!filter.performedDateTo.isNullOrBlank()) {
            conditions.add("m.performed_date <= ?")
            args.add(filter.performedDateTo.trim())
        }
        val where: String = if (conditions.isEmpty()) {
            ""
        } else {
            "WHERE " + conditions.joinToString(separator = " AND ")
        }
        val sql: String = """
            SELECT
                rp.id,
                rp.maintenance_id,
                m.vehicle_id,
                (v.name || ' — ' || b.name || ' · ' || mo.name),
                m.title,
                m.performed_date,
                rp.title,
                rp.part_or_serial,
                rp.brand,
                rp.store,
                rp.notes,
                rp.created_at
            FROM maintenance_replaced_part rp
            INNER JOIN maintenance m ON m.id = rp.maintenance_id
            INNER JOIN vehicle v ON v.id = m.vehicle_id
            INNER JOIN brand b ON b.id = v.brand_id
            INNER JOIN model mo ON mo.id = v.model_id
            $where
            ORDER BY m.performed_date DESC, rp.id DESC
        """.trimIndent()
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val result: MutableList<ReplacedPartListRow> = mutableListOf()
        val cursor = db.rawQuery(sql, args.toTypedArray())
        cursor.use { c ->
            while (c.moveToNext()) {
                result.add(
                    ReplacedPartListRow(
                        id = c.getLong(0),
                        maintenanceId = c.getLong(1),
                        vehicleId = c.getLong(2),
                        vehicleSummary = c.getString(3).orEmpty(),
                        maintenanceTitle = c.getString(4).orEmpty(),
                        performedDate = c.getString(5).orEmpty(),
                        title = c.getString(6).orEmpty(),
                        partOrSerial = c.getString(7).orEmpty(),
                        brand = if (c.isNull(8)) null else c.getString(8),
                        store = if (c.isNull(9)) null else c.getString(9),
                        notes = if (c.isNull(10)) null else c.getString(10),
                        createdAt = c.getString(11).orEmpty(),
                    ),
                )
            }
        }
        return result
    }

    /**
     * Search past replaced-part rows (by text across title, part/serial, brand, store, notes).
     * @param vehicleId when non-null, restrict to that vehicle's maintenances.
     * @param excludeRowIds rows already added as chips (by [maintenance_replaced_part.id]).
     */
    fun searchReplacedPartHistory(
        vehicleId: Long?,
        rawQuery: String,
        excludeRowIds: Set<Long>,
        limit: Int = 24,
    ): List<ReplacedPartHistoryPick> {
        val needle: String? = SqlSearch.normalizeNeedle(raw = rawQuery)
        if (needle == null) {
            return emptyList()
        }
        val conditions: MutableList<String> = mutableListOf(
            """
            INSTR(
                LOWER(
                    COALESCE(rp.title, '') || ' ' ||
                    COALESCE(rp.part_or_serial, '') || ' ' ||
                    COALESCE(rp.brand, '') || ' ' ||
                    COALESCE(rp.store, '') || ' ' ||
                    COALESCE(rp.notes, '')
                ),
                ?
            ) > 0
            """.trimIndent().replace("\n", " "),
        )
        val args: MutableList<String> = mutableListOf(needle)
        if (vehicleId != null) {
            conditions.add("m.vehicle_id = ?")
            args.add(vehicleId.toString())
        }
        if (excludeRowIds.isNotEmpty()) {
            val placeholders: String = excludeRowIds.joinToString(separator = ",") { "?" }
            conditions.add("rp.id NOT IN ($placeholders)")
            excludeRowIds.forEach { id: Long -> args.add(id.toString()) }
        }
        val where: String = "WHERE " + conditions.joinToString(separator = " AND ")
        val sql: String = """
            SELECT rp.id, rp.title, rp.part_or_serial, rp.brand, rp.store, rp.notes
            FROM maintenance_replaced_part rp
            INNER JOIN maintenance m ON m.id = rp.maintenance_id
            $where
            ORDER BY m.performed_date DESC, rp.id DESC
            LIMIT ?
        """.trimIndent()
        args.add(limit.toString())
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val result: MutableList<ReplacedPartHistoryPick> = mutableListOf()
        db.rawQuery(sql, args.toTypedArray()).use { c ->
            while (c.moveToNext()) {
                result.add(
                    ReplacedPartHistoryPick(
                        id = c.getLong(0),
                        title = c.getString(1).orEmpty(),
                        partOrSerial = c.getString(2).orEmpty(),
                        brand = if (c.isNull(3)) null else c.getString(3),
                        store = if (c.isNull(4)) null else c.getString(4),
                        notes = if (c.isNull(5)) null else c.getString(5),
                    ),
                )
            }
        }
        return result
    }

    fun insertStandalone(
        maintenanceId: Long,
        input: ReplacedPartInput,
    ): Boolean {
        val titleTrim: String = input.title.trim()
        if (titleTrim.isEmpty()) {
            return false
        }
        val db: SQLiteDatabase = dbHelper().writableDatabase
        db.beginTransaction()
        return try {
            val ok: Boolean = insertForMaintenance(db = db, maintenanceId = maintenanceId, input = input)
            if (ok) {
                db.setTransactionSuccessful()
            }
            ok
        } finally {
            db.endTransaction()
        }
    }

    fun insertForMaintenance(
        db: SQLiteDatabase,
        maintenanceId: Long,
        input: ReplacedPartInput,
    ): Boolean {
        val titleTrim: String = input.title.trim()
        if (titleTrim.isEmpty()) {
            return true
        }
        val values = ContentValues().apply {
            put("maintenance_id", maintenanceId)
            put("title", titleTrim)
            put("part_or_serial", input.partOrSerial.trim())
            if (input.brand.isNullOrBlank()) {
                putNull("brand")
            } else {
                put("brand", input.brand.trim())
            }
            if (input.store.isNullOrBlank()) {
                putNull("store")
            } else {
                put("store", input.store.trim())
            }
            if (input.notes.isNullOrBlank()) {
                putNull("notes")
            } else {
                put("notes", input.notes.trim())
            }
        }
        return db.insert("maintenance_replaced_part", null, values) != -1L
    }

    fun updatePart(
        id: Long,
        input: ReplacedPartInput,
    ): Boolean {
        val titleTrim: String = input.title.trim()
        if (titleTrim.isEmpty()) {
            return false
        }
        val values = ContentValues().apply {
            put("title", titleTrim)
            put("part_or_serial", input.partOrSerial.trim())
            if (input.brand.isNullOrBlank()) {
                putNull("brand")
            } else {
                put("brand", input.brand.trim())
            }
            if (input.store.isNullOrBlank()) {
                putNull("store")
            } else {
                put("store", input.store.trim())
            }
            if (input.notes.isNullOrBlank()) {
                putNull("notes")
            } else {
                put("notes", input.notes.trim())
            }
        }
        val db: SQLiteDatabase = dbHelper().writableDatabase
        val affected: Int = db.update(
            "maintenance_replaced_part",
            values,
            "id = ?",
            arrayOf(id.toString()),
        )
        return affected == 1
    }

    fun deletePart(id: Long): Boolean {
        val db: SQLiteDatabase = dbHelper().writableDatabase
        val affected: Int = db.delete(
            "maintenance_replaced_part",
            "id = ?",
            arrayOf(id.toString()),
        )
        return affected == 1
    }

    fun listRecentMaintenances(limit: Int = 60): List<MaintenancePickerRow> {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val sql: String = """
            SELECT m.id, m.vehicle_id,
                   (m.title || ' · ' || m.performed_date || ' — ' || v.name)
            FROM maintenance m
            INNER JOIN vehicle v ON v.id = m.vehicle_id
            ORDER BY m.performed_date DESC, m.id DESC
            LIMIT ?
        """.trimIndent()
        val result: MutableList<MaintenancePickerRow> = mutableListOf()
        val cursor = db.rawQuery(sql, arrayOf(limit.toString()))
        cursor.use { c ->
            while (c.moveToNext()) {
                result.add(
                    MaintenancePickerRow(
                        maintenanceId = c.getLong(0),
                        vehicleId = c.getLong(1),
                        label = c.getString(2).orEmpty(),
                    ),
                )
            }
        }
        return result
    }
}
