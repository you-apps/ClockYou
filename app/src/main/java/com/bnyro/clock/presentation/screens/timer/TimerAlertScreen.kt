package com.bnyro.clock.presentation.screens.timer

import android.content.res.Configuration
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.presentation.screens.ringing.RingingAlert
import com.bnyro.clock.presentation.screens.ringing.RingingTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.ZoneId

@Composable
fun TimerAlertScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onReset: () -> Unit,
    label: String? = null,
    ringingSince: Long,
    incrementSeconds: Int
) {
    RingingAlert(rememberVectorPainter(Icons.Rounded.Timer)) {
        TimerAlertControls(label, ringingSince, incrementSeconds, onDismiss, onSnooze, onReset)
    }
}

@Composable
private fun TimerAlertControls(
    label: String?,
    ringingSince: Long,
    incrementSeconds: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onReset: () -> Unit
) {
    RingingTitle(
        label,
        showSeconds = true,
        time = Instant.ofEpochMilli(ringingSince).atZone(ZoneId.systemDefault())
    )

    // the timer does not stop at zero, it goes on counting the wait for an answer
    val rung by produceState(initialValue = 0L, ringingSince) {
        while (isActive) {
            value = (System.currentTimeMillis() - ringingSince) / 1000
            delay(1000)
        }
    }
    Text(
        text = "-" + DateUtils.formatElapsedTime(rung),
        style = MaterialTheme.typography.headlineMedium
    )
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                onDismiss.invoke()
            }
        ) {
            Row(Modifier.padding(8.dp)) {
                Icon(
                    modifier = Modifier.align(alignment = Alignment.CenterVertically),
                    imageVector = Icons.Rounded.TimerOff,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dismiss),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    onSnooze.invoke()
                }
            ) {
                Text(
                    text = if (incrementSeconds == 60) {
                        stringResource(R.string.add_one_minute)
                    } else {
                        pluralStringResource(
                            R.plurals.add_seconds,
                            incrementSeconds,
                            incrementSeconds
                        )
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }
            FilledTonalButton(
                onClick = {
                    onReset.invoke()
                }
            ) {
                Row {
                    Icon(
                        modifier = Modifier.align(alignment = Alignment.CenterVertically),
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.timer_reset),
                        style = MaterialTheme.typography.titleLarge
                    )
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
    TimerAlertScreen(
        onDismiss = {},
        onSnooze = {},
        onReset = {},
        label = "Pasta",
        ringingSince = System.currentTimeMillis(),
        incrementSeconds = 60
    )
}
