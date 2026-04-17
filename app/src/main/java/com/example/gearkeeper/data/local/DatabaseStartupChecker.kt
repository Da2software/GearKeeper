package com.example.gearkeeper.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

sealed class DatabaseStartupAssessment {
    data object Ok : DatabaseStartupAssessment()

    data class IntegrityFailed(
        val detail: String,
    ) : DatabaseStartupAssessment()

    data class SchemaTooNew(
        val fileUserVersion: Int,
    ) : DatabaseStartupAssessment()
}

object DatabaseStartupChecker {

    fun assessBeforeOpen(context: Context): DatabaseStartupAssessment {
        val dbFile: File = context.getDatabasePath(GearKeeperDatabaseHelper.DATABASE_FILE_NAME)
        if (!dbFile.exists()) {
            return DatabaseStartupAssessment.Ok
        }
        val db: SQLiteDatabase = try {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )
        } catch (error: Exception) {
            return DatabaseStartupAssessment.IntegrityFailed(detail = error.message ?: "open_failed")
        }
        return try {
            val userVersion: Int = readUserVersion(db = db)
            if (userVersion > GearKeeperDatabaseHelper.SCHEMA_VERSION) {
                return DatabaseStartupAssessment.SchemaTooNew(fileUserVersion = userVersion)
            }
            val integrity: String = readIntegrityCheck(db = db)
            if (integrity != "ok") {
                DatabaseStartupAssessment.IntegrityFailed(detail = integrity)
            } else {
                DatabaseStartupAssessment.Ok
            }
        } finally {
            db.close()
        }
    }

    private fun readUserVersion(db: SQLiteDatabase): Int {
        db.rawQuery("PRAGMA user_version", null).use { cursor ->
            if (!cursor.moveToFirst()) {
                return 0
            }
            return cursor.getInt(0)
        }
    }

    private fun readIntegrityCheck(db: SQLiteDatabase): String {
        db.rawQuery("PRAGMA integrity_check", null).use { cursor ->
            if (!cursor.moveToFirst()) {
                return "empty"
            }
            return cursor.getString(0).orEmpty()
        }
    }
}
