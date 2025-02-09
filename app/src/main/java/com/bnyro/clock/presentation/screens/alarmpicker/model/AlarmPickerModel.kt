package com.bnyro.clock.presentation.screens.alarmpicker.model

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bnyro.clock.App
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.usecase.CreateUpdateDeleteAlarmUseCase
import com.bnyro.clock.navigation.NavRoutes
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

class AlarmPickerModel(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {
    private val id: String? = savedStateHandle[NavRoutes.AlarmPicker.ALARM_ID]

    private val alarmRepository = (application as App).container.alarmRepository
    private val createUpdateDeleteAlarmUseCase =
        CreateUpdateDeleteAlarmUseCase(application.applicationContext, alarmRepository)

    var alarm: Alarm

    init {
        val alarmId = id?.toLong() ?: 0L

        alarm = if (alarmId == 0L) {
            Alarm(time = TimeHelper.currentDayMillis)
        } else {
            runBlocking(Dispatchers.IO) {
                alarmRepository.getAlarmById(alarmId)!!
            }
        }
    }

    fun createAlarm(alarm: Alarm) {
        viewModelScope.launch {
            createUpdateDeleteAlarmUseCase.createAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            createUpdateDeleteAlarmUseCase.updateAlarm(alarm)
        }
    }

    fun createToast(alarm: Alarm, context: Context) {
        val millisRemainingForAlarm =
            (AlarmHelper.getAlarmTime(alarm) - System.currentTimeMillis())
        val formattedDuration =
            TimeHelper.durationToFormatted(context, millisRemainingForAlarm.milliseconds)

        Toast.makeText(
            context,
            context.resources.getString(R.string.alarm_will_play, formattedDuration),
            Toast.LENGTH_SHORT
        ).show()
    }
}