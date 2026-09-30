package com.bnyro.clock.presentation.screens.settings.components

import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.bnyro.clock.R
import kotlin.math.roundToInt

@Composable
fun AlarmVolumePreference() {
    val context = LocalContext.current
    val audioManager = remember(context) { context.getSystemService(AudioManager::class.java) }
    val minimum = remember(audioManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM)
        } else 0
    }
    val maximum = remember(audioManager) { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) }
    var volume by remember(audioManager) {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_ALARM))
    }
    DisposableEffect(context, audioManager) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            }
        }
        context.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
    LifecycleResumeEffect(audioManager) {
        volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        onPauseOrDispose { }
    }
    val title = stringResource(R.string.alarm_volume)
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = volume.toFloat(),
            onValueChange = {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it.roundToInt(), 0)
                volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            },
            valueRange = minimum.toFloat()..maximum.toFloat(),
            steps = (maximum - minimum - 1).coerceAtLeast(0),
            enabled = !audioManager.isVolumeFixed && maximum > minimum,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = title }
        )
    }
}
