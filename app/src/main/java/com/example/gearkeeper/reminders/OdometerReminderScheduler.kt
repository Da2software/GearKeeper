package com.example.gearkeeper.reminders

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object OdometerReminderScheduler {
    private const val PERIODIC_DAILY_WORK_NAME: String = "odometer-reminder-daily-check"
    private const val PERIODIC_HOURLY_WORK_NAME: String = "odometer-reminder-hourly-follow-up"
    private const val ON_DEMAND_WORK_NAME: String = "odometer-reminder-on-demand-check"

    fun ensureScheduled(context: Context): Unit {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<OdometerReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun scheduleImmediateCheck(context: Context): Unit {
        val request = OneTimeWorkRequestBuilder<OdometerReminderWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ON_DEMAND_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun ensureHourlyFollowUpScheduled(context: Context): Unit {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<OdometerReminderFollowUpWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_HOURLY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelHourlyFollowUp(context: Context): Unit {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_HOURLY_WORK_NAME)
    }
}
