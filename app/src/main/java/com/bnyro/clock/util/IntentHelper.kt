package com.bnyro.clock.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object IntentHelper {
    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        runCatching {
            context.startActivity(intent)
        }
    }
}
