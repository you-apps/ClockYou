package com.bnyro.clock.util

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

object RingtoneHelper {
    fun getDefault(context: Context): Uri? {
        return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_NOTIFICATION
            )
    }

    fun getAvailableSounds(context: Context): Map<String, Uri> {
        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_RINGTONE)
        val cursor = manager.cursor
        val list = hashMapOf<String, Uri>()
        while (cursor.moveToNext()) {
            val notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            list[notificationTitle] = manager.getRingtoneUri(cursor.position)
        }
        return list
    }
}
