package com.example.gearkeeper.data.local

/**
 * Normalizes user input for SQLite substring search with [INSTR](https://www.sqlite.org/lang_corefunc.html#instr).
 * Returns `null` when the query should not filter (blank). Otherwise returns a **lowercase** needle
 * bound as `INSTR(LOWER(concatenated_columns), ?)`.
 */
object SqlSearch {
    fun normalizeNeedle(raw: String?): String? {
        val trimmed: String = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        return trimmed.lowercase()
    }
}
