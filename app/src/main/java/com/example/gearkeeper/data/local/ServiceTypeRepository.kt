package com.example.gearkeeper.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase

data class ServiceTypeOption(
    val id: Long,
    /** Canonical English name (unique, used for custom entries and sorting). */
    val name: String,
    /** Spanish display name for seeded types; null or blank falls back to [name]. */
    val nameEs: String?,
)

data class ServiceTypeCatalogItem(
    val id: Long,
    val name: String,
    val nameEs: String?,
    val isSeeded: Boolean,
)

enum class ServiceTypeDeleteResult {
    Success,
    NotFoundOrBuiltIn,
    InUseByMaintenance,
    UnknownFailure,
}

class ServiceTypeRepository(
    context: android.content.Context,
) {
    private val appContext: android.content.Context = context.applicationContext

    private fun dbHelper(): GearKeeperDatabaseHelper = GearKeeperDatabaseSingleton.get(context = appContext)

    fun getAllOrderedByName(): List<ServiceTypeOption> {
        return getAllCatalogItemsOrderedByName(searchQuery = null).map { item: ServiceTypeCatalogItem ->
            ServiceTypeOption(id = item.id, name = item.name, nameEs = item.nameEs)
        }
    }

    /**
     * @param searchQuery optional user text; blank returns full catalog (ordered by name).
     */
    fun getAllCatalogItemsOrderedByName(searchQuery: String? = null): List<ServiceTypeCatalogItem> {
        val needle: String? = SqlSearch.normalizeNeedle(raw = searchQuery)
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val result: MutableList<ServiceTypeCatalogItem> = mutableListOf()
        val sql: String
        val bindArgs: Array<String>?
        if (needle == null) {
            sql = "SELECT id, name, name_es, is_seeded FROM service_type ORDER BY name COLLATE NOCASE ASC"
            bindArgs = null
        } else {
            sql = """
                SELECT id, name, name_es, is_seeded FROM service_type
                WHERE INSTR(LOWER(name), ?) > 0
                   OR INSTR(LOWER(IFNULL(name_es, '')), ?) > 0
                ORDER BY name COLLATE NOCASE ASC
            """.trimIndent()
            bindArgs = arrayOf(needle, needle)
        }
        val cursor = db.rawQuery(sql, bindArgs)
        cursor.use { c ->
            while (c.moveToNext()) {
                result.add(
                    ServiceTypeCatalogItem(
                        id = c.getLong(0),
                        name = c.getString(1),
                        nameEs = if (c.isNull(2)) null else c.getString(2),
                        isSeeded = c.getInt(3) != 0,
                    ),
                )
            }
        }
        return result
    }

    fun getCatalogItem(id: Long): ServiceTypeCatalogItem? {
        val db: SQLiteDatabase = dbHelper().readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, name_es, is_seeded FROM service_type WHERE id = ?",
            arrayOf(id.toString()),
        )
        cursor.use { c ->
            if (!c.moveToFirst()) {
                return null
            }
            return ServiceTypeCatalogItem(
                id = c.getLong(0),
                name = c.getString(1),
                nameEs = if (c.isNull(2)) null else c.getString(2),
                isSeeded = c.getInt(3) != 0,
            )
        }
    }

    /**
     * @return new row id, or null if insert failed or name empty / duplicate.
     */
    fun insertCustomName(name: String): Long? {
        val trimmed: String = name.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val values: ContentValues = ContentValues().apply {
            put("name", trimmed)
            put("is_seeded", 0)
        }
        return try {
            val db: SQLiteDatabase = dbHelper().writableDatabase
            val id: Long = db.insert("service_type", null, values)
            if (id == -1L) {
                null
            } else {
                id
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Rename a **custom** (non-seeded) service type. Built-in types cannot be updated.
     */
    fun updateCustomServiceTypeName(id: Long, newName: String): Boolean {
        val trimmed: String = newName.trim()
        if (trimmed.isEmpty()) {
            return false
        }
        return try {
            val db: SQLiteDatabase = dbHelper().writableDatabase
            val updated: Int = db.update(
                "service_type",
                ContentValues().apply { put("name", trimmed) },
                "id = ? AND is_seeded = 0",
                arrayOf(id.toString()),
            )
            updated == 1
        } catch (_: SQLiteConstraintException) {
            false
        }
    }

    /**
     * Remove a **custom** service type. Seeded types are never deleted.
     * Fails with [ServiceTypeDeleteResult.InUseByMaintenance] if referenced by past maintenance.
     */
    fun deleteCustomServiceType(id: Long): ServiceTypeDeleteResult {
        val db: SQLiteDatabase = dbHelper().writableDatabase
        val cursor = db.rawQuery(
            "SELECT is_seeded FROM service_type WHERE id = ?",
            arrayOf(id.toString()),
        )
        cursor.use { c ->
            if (!c.moveToFirst()) {
                return ServiceTypeDeleteResult.NotFoundOrBuiltIn
            }
            if (c.getInt(0) != 0) {
                return ServiceTypeDeleteResult.NotFoundOrBuiltIn
            }
        }
        return try {
            val removed: Int = db.delete("service_type", "id = ?", arrayOf(id.toString()))
            if (removed == 1) {
                ServiceTypeDeleteResult.Success
            } else {
                ServiceTypeDeleteResult.UnknownFailure
            }
        } catch (_: SQLiteConstraintException) {
            ServiceTypeDeleteResult.InUseByMaintenance
        }
    }
}
