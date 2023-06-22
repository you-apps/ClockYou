package com.bnyro.clock.obj

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class ScheduledObject(
    var id: Int = 0,
    var label: MutableState<String?> = mutableStateOf(null),
    var currentPosition: MutableState<Int> = mutableStateOf(0),
    var state: MutableState<WatchState> = mutableStateOf(WatchState.IDLE)
)
