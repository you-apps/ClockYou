package com.bnyro.clock.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.os.UserManagerCompat
import com.bnyro.clock.App
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val isValidAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED || action == Intent.ACTION_BOOT_COMPLETED
        } else {
            action == Intent.ACTION_BOOT_COMPLETED
        }

        if (!isValidAction) return
        val safeContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (UserManagerCompat.isUserUnlocked(context)) {
                context
            } else {
                context.createDeviceProtectedStorageContext()
            }
        } else {
            context
        }
        val alarmRepository = (safeContext.applicationContext as App).container.alarmRepository

        runBlocking(Dispatchers.IO) {
            try {
                val alarms = alarmRepository.getAlarms()
                alarms.forEach { alarm ->
                    AlarmHelper.enqueue(safeContext, alarm)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}