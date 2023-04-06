package com.bnyro.clock

import android.app.Application
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.util.NotificationHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        DatabaseHolder.init(this)
        NotificationHelper.createNotificationChannels(this)
    }
}
