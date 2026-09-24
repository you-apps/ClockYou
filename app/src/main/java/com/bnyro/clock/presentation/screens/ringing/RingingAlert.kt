package com.bnyro.clock.presentation.screens.ringing

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.ThemeUtil
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.ZonedDateTime

/**
 * The shape every ringing screen takes: a dark page carrying the icon of whatever is ringing beside
 * the controls that answer it, side by side when the phone is on its side.
 */
@Composable
fun RingingAlert(icon: Painter, controls: @Composable ColumnScope.() -> Unit) {
    val settingsModel: SettingsModel = viewModel()
    ClockYouTheme(
        darkTheme = true,
        customColorScheme = ThemeUtil.getSchemeFromSeed(
            settingsModel.customColor,
            true
        )
    ) {
        val orientation = LocalConfiguration.current.orientation
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (orientation == ORIENTATION_PORTRAIT) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RingingIcon(icon)
                    controls()
                }
            } else {
                Row {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RingingIcon(icon)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(3f),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = controls
                    )
                }
            }
        }
    }
}

/**
 * The time a ringing screen leads with, which is the time an alarm was set for and the moment a
 * timer finished, over the name of whatever is ringing, which a reader woken by it reads first.
 */
@Composable
fun RingingTitle(label: String?, showSeconds: Boolean = false, time: ZonedDateTime? = null) {
    val context = LocalContext.current
    val shownTime = if (time != null) {
        TimeHelper.formatTime(context, time, showSeconds)
    } else {
        val now by produceState(
            initialValue = TimeHelper.formatTime(
                context,
                TimeHelper.getTimeByZone(),
                showSeconds
            ),
            showSeconds
        ) {
            while (isActive) {
                value = TimeHelper.formatTime(
                    context,
                    TimeHelper.getTimeByZone(),
                    showSeconds
                )
                delay(1000)
            }
        }
        now
    }
    Text(
        text = shownTime,
        style = MaterialTheme.typography.displayMedium
    )
    label?.let {
        Text(text = it, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun RingingIcon(icon: Painter) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10F,
        targetValue = 10F,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    val offset by infiniteTransition.animateFloat(
        initialValue = 10F,
        targetValue = -10F,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = Ease),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset(y = offset.dp)
            .rotate(rotation)
    ) {
        Image(
            modifier = Modifier.size(250.dp),
            painter = icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
    }
}
