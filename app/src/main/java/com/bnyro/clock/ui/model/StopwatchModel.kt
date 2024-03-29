package com.bnyro.clock.ui.model

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.bnyro.clock.obj.TimeObject
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.services.StopwatchService
import com.bnyro.clock.util.TimeHelper

class StopwatchModel : ViewModel() {
    /**
     * List of laps with overall time <> lap time
     */
    val rememberedTimeStamps = mutableStateListOf<Pair<TimeObject, TimeObject>>()
    var currentPosition by mutableStateOf(0)
    var state: WatchState by mutableStateOf(WatchState.IDLE)

    private fun startStopwatch(context: Context) {
        val intent = Intent(context, StopwatchService::class.java)
        ContextCompat.startForegroundService(context, intent)

        rememberedTimeStamps.clear()
        val startIntent = Intent(StopwatchService.STOPWATCH_INTENT_ACTION).putExtra(
            StopwatchService.ACTION_EXTRA_KEY,
            StopwatchService.ACTION_START
        )
        context.sendBroadcast(startIntent)
    }

    fun onLapClicked() {
        val overallTime = TimeHelper.millisToTime(currentPosition.toLong())
        if (rememberedTimeStamps.isNotEmpty()) {
            val lastLap = rememberedTimeStamps.last()
            rememberedTimeStamps.add(Pair(overallTime, overallTime - lastLap.first))
        } else {
            rememberedTimeStamps.add(Pair(overallTime, overallTime))
        }

    }

    fun pauseResumeStopwatch(context: Context) {
        when (state) {
            WatchState.IDLE -> startStopwatch(context)
            else -> {
                val pauseResumeIntent = Intent(StopwatchService.STOPWATCH_INTENT_ACTION).putExtra(
                    StopwatchService.ACTION_EXTRA_KEY,
                    StopwatchService.ACTION_PAUSE_RESUME
                )
                context.sendBroadcast(pauseResumeIntent)
            }
        }
    }

    fun stopStopwatch(context: Context) {
        val stopIntent = Intent(StopwatchService.STOPWATCH_INTENT_ACTION).putExtra(
            StopwatchService.ACTION_EXTRA_KEY,
            StopwatchService.ACTION_STOP
        )
        context.sendBroadcast(stopIntent)
    }
}
