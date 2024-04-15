package com.bnyro.clock.domain.model

import android.os.Parcelable
import androidx.compose.runtime.mutableStateOf
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimerDescriptor(
    var id: Int = 0,
    var currentPosition: Int = 0,
) : Parcelable {
    fun asScheduledObject(): TimerObject {
        return TimerObject(
            id = id,
            currentPosition = mutableStateOf(currentPosition),
        )
    }
}