package com.bnyro.clock.domain.model

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class TimerObject(
    var id: Int = 0,
    var label: MutableState<String?> = mutableStateOf(null),
    var currentPosition: MutableState<Int> = mutableStateOf(0),
    val initialPosition: Int = currentPosition.value,
    var state: MutableState<WatchState> = mutableStateOf(WatchState.IDLE),
    var ringtone: Uri? = null,
    var vibrate: Boolean = false
)
