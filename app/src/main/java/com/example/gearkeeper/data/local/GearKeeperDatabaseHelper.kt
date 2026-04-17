package com.example.gearkeeper.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GearKeeperDatabaseHelper(
    context: Context,
) : SQLiteOpenHelper(context, DATABASE_FILE_NAME, null, SCHEMA_VERSION) {
    private val appContext: Context = context.applicationContext

    override fun onCreate(db: SQLiteDatabase): Unit {
        db.beginTransaction()
        try {
            runSqlAsset(db = db, assetPath = "db/schema.sql")
            runSqlAsset(db = db, assetPath = "db/seed_brands_models.sql")
            runSqlAsset(db = db, assetPath = "db/seed_popular_services.sql")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ): Unit {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE vehicle ADD COLUMN bin_number TEXT")
            db.execSQL("ALTER TABLE vehicle ADD COLUMN doors INTEGER")
            db.execSQL("ALTER TABLE vehicle ADD COLUMN transmission_subtype TEXT")
        }
        if (oldVersion < 3) {
            db.beginTransaction()
            try {
                runSqlAsset(db = db, assetPath = "db/migration_v3_maintenance.sql")
                runSqlAsset(db = db, assetPath = "db/seed_popular_services.sql")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 4) {
            db.beginTransaction()
            try {
                runSqlAsset(db = db, assetPath = "db/seed_popular_services.sql")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 5) {
            db.beginTransaction()
            try {
                runSqlAsset(db = db, assetPath = "db/migration_v5_odometer_reading.sql")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE service_type ADD COLUMN name_es TEXT")
            db.beginTransaction()
            try {
                runSqlAsset(db = db, assetPath = "db/migration_v6_service_type_name_es.sql")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 7) {
            db.beginTransaction()
            try {
                runSqlAsset(db = db, assetPath = "db/migration_v7_planned_maintenance.sql")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 8) {
            db.beginTransaction()
            try {
                runSqlAsset(db = db, assetPath = "db/migration_v8_replaced_parts.sql")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 9) {
            db.beginTransaction()
            try {
                runSqlAsset(db = db, assetPath = "db/migration_v9_odometer_reminders.sql")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun runSqlAsset(
        db: SQLiteDatabase,
        assetPath: String,
    ): Unit {
        val script: String = appContext.assets.open(assetPath).bufferedReader().use { reader ->
            reader.readText()
        }
        val statements: List<String> = script
            .lineSequence()
            .filter { line: String -> !line.trim().startsWith("--") }
            .joinToString(separator = "\n")
            .split(";")
            .map { statement: String -> statement.trim() }
            .filter { statement: String -> statement.isNotBlank() }
        statements.forEach { statement: String ->
            val upper: String = statement.trimStart().uppercase()
            if (upper == "BEGIN TRANSACTION" || upper == "COMMIT" || upper == "ROLLBACK") {
                return@forEach
            }
            db.execSQL(statement)
        }
    }

    companion object {
        const val DATABASE_FILE_NAME: String = "gearkeeper.db"
        /** Matches `PRAGMA user_version` after migrations. */
        const val SCHEMA_VERSION: Int = 9
    }
}

