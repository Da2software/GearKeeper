package com.example.gearkeeper.ui.util

import android.app.DatePickerDialog
import android.content.Context
import java.util.Calendar
import java.util.Locale

/**
 * Opens the platform [DatePickerDialog] and returns the chosen day as `YYYY-MM-DD` in [Locale.US] (SQLite-friendly).
 */
fun showIsoDatePickerDialog(
    context: Context,
    initialIsoDate: String,
    onDatePicked: (isoDate: String) -> Unit,
): Unit {
    val cal: Calendar = Calendar.getInstance()
    runCatching {
        val head: String = initialIsoDate.trim().take(10)
        val parts: List<String> = head.split("-")
        if (parts.size == 3) {
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
        }
    }
    val year: Int = cal.get(Calendar.YEAR)
    val month: Int = cal.get(Calendar.MONTH)
    val day: Int = cal.get(Calendar.DAY_OF_MONTH)
    DatePickerDialog(
        context,
        { _, pickedYear: Int, pickedMonth: Int, dayOfMonth: Int ->
            val iso: String = String.format(
                Locale.US,
                "%04d-%02d-%02d",
                pickedYear,
                pickedMonth + 1,
                dayOfMonth,
            )
            onDatePicked(iso)
        },
        year,
        month,
        day,
    ).show()
}
