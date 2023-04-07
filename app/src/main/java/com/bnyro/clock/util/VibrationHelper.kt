package com.bnyro.clock.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationHelper {
    private val VIBRATE_PATTERN = longArrayOf(500, 500, 500, 500, 500, 500)

    fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATE_PATTERN, 0)
        }
    }
}
