package com.example.gearkeeper.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase

object GearKeeperDatabaseSingleton {
    @Volatile
    private var helper: GearKeeperDatabaseHelper? = null

    fun get(context: Context): GearKeeperDatabaseHelper {
        val app: Context = context.applicationContext
        return helper ?: synchronized(this) {
            helper ?: GearKeeperDatabaseHelper(context = app).also { created: GearKeeperDatabaseHelper ->
                helper = created
            }
        }
    }

    fun checkpoint(context: Context): Unit {
        val db: SQLiteDatabase = get(context = context).writableDatabase
        db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { cursor ->
            cursor.moveToFirst()
        }
    }

    fun closeAndReset(): Unit {
        synchronized(this) {
            helper?.close()
            helper = null
        }
    }
}
