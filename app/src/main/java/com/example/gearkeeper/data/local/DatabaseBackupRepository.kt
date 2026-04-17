package com.example.gearkeeper.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DatabaseBackupRepository(
    context: Context,
) {
    private val appContext: Context = context.applicationContext

    fun exportSourceFileToUri(
        sourceFile: File,
        uri: Uri,
    ): Result<Unit> {
        return runCatching {
            if (!sourceFile.exists()) {
                error("Source file not found")
            }
            appContext.contentResolver.openOutputStream(uri)?.use { output ->
                sourceFile.inputStream().use { input: InputStream ->
                    input.copyTo(output)
                }
            } ?: error("Could not open export destination")
        }
    }

    fun exportToUri(uri: Uri): Result<Unit> {
        return runCatching {
            GearKeeperDatabaseSingleton.checkpoint(context = appContext)
            val dbFile: File = appContext.getDatabasePath(GearKeeperDatabaseHelper.DATABASE_FILE_NAME)
            if (!dbFile.exists()) {
                error("Database file not found")
            }
            appContext.contentResolver.openOutputStream(uri)?.use { output ->
                dbFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: error("Could not open export destination")
        }
    }

    fun importFromUri(uri: Uri): Result<Unit> {
        return runCatching {
            GearKeeperDatabaseSingleton.closeAndReset()
            val dbFile: File = appContext.getDatabasePath(GearKeeperDatabaseHelper.DATABASE_FILE_NAME)
            dbFile.parentFile?.mkdirs()
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            } ?: error("Could not read selected file")
            File(dbFile.absolutePath + "-wal").delete()
            File(dbFile.absolutePath + "-shm").delete()
            verifySqliteHeader(file = dbFile)
        }
    }

    private fun verifySqliteHeader(file: File): Unit {
        val header: ByteArray = ByteArray(size = 16)
        file.inputStream().use { stream ->
            if (stream.read(header) != 16) {
                error("File is too small to be a SQLite database")
            }
        }
        val magic: String = String(header, Charsets.US_ASCII)
        if (!magic.startsWith(prefix = "SQLite format 3")) {
            error("Selected file is not a SQLite database backup")
        }
    }
}
