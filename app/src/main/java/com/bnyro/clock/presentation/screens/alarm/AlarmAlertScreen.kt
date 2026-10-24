package com.bnyro.clock.presentation.screens.alarm

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.presentation.screens.ringing.RingingAlert
import com.bnyro.clock.presentation.screens.ringing.RingingTitle
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun AlarmAlertScreen(
    onDismiss: () -> Unit,
    onSnooze: (minutes: Int) -> Unit,
    label: String? = null,
    snoozeEnabled: Boolean,
    snoozeTime: Int,
    alarmTimeMillis: Long
) {
    RingingAlert(painterResource(id = R.drawable.ic_alarm)) {
        AlarmControls(label, alarmTimeMillis, snoozeTime, snoozeEnabled, onSnooze, onDismiss)
    }
}

@Composable
private fun AlarmControls(
    label: String?,
    alarmTimeMillis: Long,
    snoozeTime: Int,
    snoozeEnabled: Boolean,
    onSnooze: (minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    RingingTitle(
        label,
        time = LocalDate.now()
            .atTime(LocalTime.ofSecondOfDay(alarmTimeMillis / 1000))
            .atZone(ZoneId.systemDefault())
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
        if (snoozeEnabled) {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var snoozeMins by remember { mutableIntStateOf(snoozeTime) }
                FilledTonalIconButton(onClick = {
                    snoozeMins -= 1
                    if (snoozeMins <= 0) snoozeMins = 1
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = stringResource(R.string.subtract_minutes, 1)
                    )
                }
                FilledTonalButton(
                    onClick = {
                        onSnooze.invoke(snoozeMins)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.snooze_minutes, snoozeMins),
                        style = MaterialTheme.typography.titleLarge
                    )

                }
                FilledTonalIconButton(onClick = { snoozeMins += 5 }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_minutes, 5)
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
    AlarmAlertScreen(onDismiss = {}, onSnooze = {}, snoozeTime = 10, label = "Test Alarm",
        snoozeEnabled = true, alarmTimeMillis = 7 * 60 * 60 * 1000L + 30 * 60 * 1000L
    )
}

@Preview(
    showBackground = true
)
@Composable
private fun ControllerPreview() {
    AlarmControls(label = "Alarm", alarmTimeMillis = 7 * 60 * 60 * 1000L + 30 * 60 * 1000L, snoozeTime = 10, snoozeEnabled = true, onSnooze = {}) {

    }
}
