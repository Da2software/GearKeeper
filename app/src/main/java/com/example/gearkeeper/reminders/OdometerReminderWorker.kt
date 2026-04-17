package com.example.gearkeeper.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gearkeeper.data.local.OdometerReminderRepository

class OdometerReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val repository: OdometerReminderRepository =
                OdometerReminderRepository(context = applicationContext)
            val pendingCount: Int = repository.getPendingVehicles().size
            if (pendingCount > 0) {
                OdometerReminderNotification.showPendingVehicleReminder(
                    context = applicationContext,
                    pendingVehicleCount = pendingCount,
                )
                OdometerReminderScheduler.ensureHourlyFollowUpScheduled(context = applicationContext)
            } else {
                OdometerReminderNotification.cancel(context = applicationContext)
                OdometerReminderScheduler.cancelHourlyFollowUp(context = applicationContext)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
