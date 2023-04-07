package com.bnyro.clock.util

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

object RingtoneHelper {
    fun getUri(context: Context): Uri? {
        return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_NOTIFICATION
            )
    }
}
