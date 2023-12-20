package com.bnyro.clock.ui.model

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bnyro.clock.App
import com.bnyro.clock.util.RingtoneHelper

class RingingToneModel(val app: App) : ViewModel() {
    var sounds =
        RingtoneHelper.getAvailableSounds(app.applicationContext).toList().sortedBy { it.first }
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as App
                RingingToneModel(application)
            }
        }
    }
}
