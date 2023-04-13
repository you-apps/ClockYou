package com.bnyro.clock

import android.app.Application
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.util.NotificationHelper
import com.bnyro.clock.util.Preferences

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        DatabaseHolder.init(this)
        Preferences.init(this)
        NotificationHelper.createNotificationChannels(this)
    }
}
