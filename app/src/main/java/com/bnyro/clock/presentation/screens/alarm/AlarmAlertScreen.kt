package com.bnyro.clock.presentation.screens.alarm

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.presentation.screens.ringing.RingingAlert
import com.bnyro.clock.presentation.screens.ringing.RingingTitle
import com.bnyro.clock.util.TimeHelper
import java.time.Instant
import java.time.ZoneId

@Composable
fun AlarmAlertScreen(
    onDismiss: () -> Unit,
    onSnooze: (minutes: Int) -> Unit,
    label: String? = null,
    snoozeEnabled: Boolean,
    snoozeTime: Int,
    ringingSince: Long
) {
    val context = LocalContext.current
    RingingAlert(
        icon = painterResource(R.drawable.ic_alarm),
        onDismiss = onDismiss,
        title = {
            RingingTitle(
                label = label,
                ringingTime = TimeHelper.formatTime(context, Instant.ofEpochMilli(ringingSince).atZone(ZoneId.systemDefault()))
            )
        },
        controls = {
            if (snoozeEnabled) {
                var snoozeMinutes by rememberSaveable(ringingSince, snoozeTime) {
                    mutableIntStateOf(snoozeTime)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { snoozeMinutes -= 1 },
                        enabled = snoozeMinutes > 1,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 6.dp, bottomEnd = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Remove, stringResource(R.string.subtract_minutes, 1))
                    }
                    FilledTonalButton(
                        onClick = { onSnooze(snoozeMinutes) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        BasicText(
                            text = stringResource(R.string.ringing_snooze_minutes, snoozeMinutes),
                            style = MaterialTheme.typography.titleSmall.copy(color = LocalContentColor.current, textAlign = TextAlign.Center),
                            maxLines = 1,
                            softWrap = false,
                            autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 14.sp, stepSize = 1.sp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { snoozeMinutes += 1 },
                        enabled = snoozeMinutes < 60,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 28.dp, bottomEnd = 28.dp)
                    ) {
                        Icon(Icons.Rounded.Add, stringResource(R.string.add_minutes, 1))
                    }
                }
            }
        }
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=390dp,height=844dp",
    showSystemUi = true
)
@Composable
private fun DefaultPreview() {
    AlarmAlertScreen(
        onDismiss = {},
        onSnooze = {},
        label = "Wake up",
        snoozeEnabled = true,
        snoozeTime = 10,
        ringingSince = System.currentTimeMillis() - 60_000
    )
}
