package com.bnyro.clock.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.bnyro.clock.App
import com.bnyro.clock.domain.usecase.CreateUpdateDeleteAlarmUseCase
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.runBlocking
//i really am going overboard with recievers but whatever they cool af
class DismissUpcomingReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getLongExtra(AlarmHelper.EXTRA_ID, -1)?.takeIf { it != -1L } ?: return
        val alarmRepository = (context.applicationContext as App).container.alarmRepository

        runBlocking {
            val alarm = alarmRepository.getAlarmById(id) ?: return@runBlocking
            CreateUpdateDeleteAlarmUseCase(context, alarmRepository).dismissUpcomingAlarm(alarm)
        }
    }
}
