package com.bnyro.clock.presentation.screens.timer

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.presentation.screens.ringing.RingingAlert
import com.bnyro.clock.presentation.screens.ringing.RingingTitle
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.ZoneId

@Composable
fun TimerAlertScreen(
    onDismiss: () -> Unit,
    onSnooze: (seconds: Int) -> Unit,
    onReset: () -> Unit,
    label: String? = null,
    ringingSince: Long,
    incrementSeconds: Int
) {
    val context = LocalContext.current
    val ringingSeconds by produceState(
        initialValue = ((System.currentTimeMillis() - ringingSince) / 1000).coerceAtLeast(0),
        ringingSince
    ) {
        while (isActive) {
            value = ((System.currentTimeMillis() - ringingSince) / 1000).coerceAtLeast(0)
            delay(1000)
        }
    }
    RingingAlert(
        icon = rememberVectorPainter(Icons.Rounded.Timer),
        onDismiss = onDismiss,
        title = {
            RingingTitle(
                label = label,
                ringingTime = TimeHelper.formatTime(context, Instant.ofEpochMilli(ringingSince).atZone(ZoneId.systemDefault()), true),
                showSeconds = true,
                ringingSeconds = ringingSeconds
            )
        },
        controls = {
            if (incrementSeconds > 0) {
                var addedSeconds by rememberSaveable(ringingSince, incrementSeconds) {
                    mutableIntStateOf(incrementSeconds)
                }
                val step = if (addedSeconds < 120) 1 else 60
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedIconButton(
                        onClick = onReset,
                        modifier = Modifier.padding(end = 4.dp).size(56.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, stringResource(R.string.timer_reset))
                    }
                    FilledTonalIconButton(
                        onClick = { addedSeconds = (addedSeconds - step).coerceAtLeast(1) },
                        enabled = addedSeconds > 1,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 6.dp, bottomEnd = 6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Remove,
                            if (step == 1) stringResource(R.string.subtract_seconds, 1)
                            else stringResource(R.string.subtract_minutes, 1)
                        )
                    }
                    FilledTonalButton(
                        onClick = { onSnooze(addedSeconds) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        BasicText(
                            text = if (addedSeconds < 120) {
                                stringResource(R.string.ringing_add_seconds, addedSeconds)
                            } else {
                                stringResource(R.string.ringing_add_minutes, addedSeconds / 60)
                            },
                            style = MaterialTheme.typography.titleSmall.copy(color = LocalContentColor.current, textAlign = TextAlign.Center),
                            maxLines = 1,
                            softWrap = false,
                            autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 14.sp, stepSize = 1.sp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { addedSeconds = (addedSeconds + step).coerceAtMost(3600) },
                        enabled = addedSeconds < 3600,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 28.dp, bottomEnd = 28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            if (step == 1) pluralStringResource(R.plurals.add_seconds, 1, 1)
                            else stringResource(R.string.add_minutes, 1)
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.timer_reset),
                        style = MaterialTheme.typography.titleSmall
                    )
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
    TimerAlertScreen(
        onDismiss = {},
        onSnooze = {},
        onReset = {},
        label = "Pasta",
        ringingSince = System.currentTimeMillis() - 66_000,
        incrementSeconds = 30
    )
}
