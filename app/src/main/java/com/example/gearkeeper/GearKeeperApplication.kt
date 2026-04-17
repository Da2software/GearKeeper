package com.example.gearkeeper

import android.app.Application
import com.example.gearkeeper.data.preferences.AppLocaleCoordinator
import com.example.gearkeeper.data.preferences.UserPreferencesRepository
import com.example.gearkeeper.reminders.OdometerReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GearKeeperApplication : Application() {
    override fun onCreate(): Unit {
        super.onCreate()
        runBlocking {
            val repository: UserPreferencesRepository = UserPreferencesRepository(context = this@GearKeeperApplication)
            val languageTag: String = repository.userPreferences.first().languageTag
            AppLocaleCoordinator.apply(languageTag = languageTag)
        }
        OdometerReminderScheduler.ensureScheduled(context = this)
        OdometerReminderScheduler.scheduleImmediateCheck(context = this)
    }
}
