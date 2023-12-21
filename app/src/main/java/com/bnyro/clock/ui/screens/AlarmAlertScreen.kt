package com.bnyro.clock.ui.screens

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.ThemeUtil

@Composable
fun AlarmAlertScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    label: String? = null,
    snoozeEnabled: Boolean
) {
    val settingsModel: SettingsModel = viewModel()
    ClockYouTheme(
        darkTheme = true,
        customColorScheme = ThemeUtil.getSchemeFromSeed(
            settingsModel.customColor,
            true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        painter = painterResource(id = R.drawable.ic_alarm),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
                label?.let {
                    Text(text = it, style = MaterialTheme.typography.headlineMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (snoozeEnabled) {
                        OutlinedButton(
                            onClick = {
                                onSnooze.invoke()
                            }
                        ) {
                            Row(Modifier.padding(8.dp)) {
                                Icon(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    imageVector = Icons.Rounded.Snooze,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.snooze),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            onDismiss.invoke()
                        }
                    ) {
                        Row(Modifier.padding(8.dp)) {
                            Icon(
                                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                                imageVector = Icons.Rounded.AlarmOff,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.dismiss),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=411dp,height=891dp",
    showSystemUi = true
)
@Composable
private fun DefaultPreview() {
    AlarmAlertScreen(onDismiss = {}, onSnooze = {}, label = "Test Alarm", true)
}
