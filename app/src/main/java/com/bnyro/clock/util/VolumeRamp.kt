package com.bnyro.clock.util

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

/**
 * The rise a ringing sound takes from its quietest to its loudest over the time the reader asked
 * for, so that an alarm or a timer can arrive gently rather than all at once. A rise of no time is
 * no rise at all: the sound starts where it means to stay.
 */
class VolumeRamp(private val player: MediaPlayer, private val seconds: Int) {
    private val handler = Handler(Looper.getMainLooper())
    private var volume = START_VOLUME

    private val rise = object : Runnable {
        override fun run() {
            player.setVolume(volume, volume)
            if (volume >= MAX_VOLUME) return

            volume = (volume + STEP).coerceAtMost(MAX_VOLUME)
            handler.postDelayed(this, seconds * 1000L / STEPS)
        }
    }

    fun start() {
        if (seconds <= 0) return

        volume = START_VOLUME
        handler.post(rise)
    }

    fun cancel() {
        handler.removeCallbacks(rise)
    }

    companion object {
        private const val START_VOLUME = 0.1f
        private const val MAX_VOLUME = 1.0f
        private const val STEP = 0.05f
        private const val STEPS = 18
    }
}
