package com.bnyro.clock.services

import android.app.Service
import android.content.Intent
import android.os.Binder

class TimerService : Service() {
    private val binder = LocalBinder()
    override fun onBind(intent: Intent) = binder
    inner class LocalBinder : Binder() {
        fun getService() = this@TimerService
    }
}
