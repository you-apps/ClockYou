package com.bnyro.clock

import android.app.Application
import com.bnyro.clock.data.database.AppDatabase
import com.bnyro.clock.util.NotificationHelper
import com.bnyro.clock.util.Preferences

class App : Application() {
    lateinit var container: AppContainer
    private val database by lazy { AppDatabase.getDatabase(this) }
    override fun onCreate() {
        super.onCreate()

        Preferences.init(this)
        NotificationHelper.createStaticNotificationChannels(this)

        container = AppContainer(database)
    }
}
