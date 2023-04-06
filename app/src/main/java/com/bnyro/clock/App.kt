package com.bnyro.clock

import android.app.Application
import com.bnyro.clock.util.Preferences

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Preferences.init(this)
    }
}
