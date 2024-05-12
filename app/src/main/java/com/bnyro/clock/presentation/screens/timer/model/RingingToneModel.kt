package com.bnyro.clock.presentation.screens.timer.model

import android.app.Application
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.bnyro.clock.util.RingtoneHelper

class RingingToneModel(application: Application) : AndroidViewModel(application) {
    var sounds =
        RingtoneHelper().getAvailableSounds(application.applicationContext).toList()
            .sortedBy { it.first }
        private set
    private var currentlyPlayingRingtone: Ringtone? = null

    fun playRingingTone(context: Context, uri: Uri) {
        if (currentlyPlayingRingtone?.isPlaying == true) {
            stopRinging()
        }

        currentlyPlayingRingtone = RingtoneManager.getRingtone(context, uri)
        currentlyPlayingRingtone?.play()
    }

    fun stopRinging() {
        currentlyPlayingRingtone?.stop()
        currentlyPlayingRingtone = null
    }
}
