package com.bnyro.clock

import android.app.Application
import com.bnyro.clock.db.DatabaseHolder

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        DatabaseHolder.init(this)
    }
}
