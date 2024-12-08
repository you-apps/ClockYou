package com.bnyro.clock.presentation.screens.timer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.TimerObject
import com.bnyro.clock.domain.model.WatchState
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.features.RingtonePickerDialog
import com.bnyro.clock.presentation.screens.timer.model.TimerModel
import com.bnyro.clock.util.TimeHelper
import com.bnyro.clock.util.extensions.addZero
import java.time.ZonedDateTime

@Composable
fun TimerItem(obj: TimerObject, timerModel: TimerModel) {
    val context = LocalContext.current
    val hours = obj.currentPosition.value / 3600000
    val minutes = (obj.currentPosition.value % 3600000) / 60000
    val seconds = (obj.currentPosition.value % 60000) / 1000

    var showLabelEditor by remember {
        mutableStateOf(false)
    }
    var showRingtoneEditor by remember {
        mutableStateOf(false)
    }

    ElevatedCard(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val colorTextLowerAlpha = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

                Column {
                    obj.label.value?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorTextLowerAlpha,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "$hours:${minutes.addZero()}:${seconds.addZero()}",
                        style = MaterialTheme.typography.displaySmall
                    )
                    AnimatedVisibility(obj.state.value == WatchState.RUNNING) {
                        Row(
                            modifier = Modifier
                                .offset(x = (-6).dp, y = (2.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showRingtoneEditor = true
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colorTextLowerAlpha
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = TimeHelper.formatTime(
                                    ZonedDateTime.now().plusHours(hours.toLong())
                                        .plusMinutes(minutes.toLong()).plusSeconds(seconds.toLong())
                                ),
                                color = colorTextLowerAlpha,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                ClickableIcon(imageVector = Icons.Default.Edit) {
                    showLabelEditor = true
                }
                ClickableIcon(imageVector = Icons.Default.Close) {
                    timerModel.stopTimer(context, obj.id)
                }
                FilledIconButton(
                    onClick = {
                        timerModel.pauseResumeTimer(context, obj.id)
                    }
                ) {
                    Icon(
                        imageVector = if (obj.state.value == WatchState.RUNNING) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = null
                    )
                }
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .height(8.dp),
                progress = obj.currentPosition.value / obj.initialPosition.toFloat(),
                strokeCap = StrokeCap.Round
            )
        }
    }

    if (showLabelEditor) {
        var newLabel by remember {
            mutableStateOf(obj.label.value.orEmpty())
        }

        AlertDialog(
            onDismissRequest = { showLabelEditor = false },
            confirmButton = {
                DialogButton(android.R.string.ok) {
                    timerModel.updateLabel(obj.id, newLabel)
                    newLabel = ""
                    showLabelEditor = false
                }
            },
            dismissButton = {
                DialogButton(android.R.string.cancel) {
                    newLabel = ""
                    showLabelEditor = false
                }
            },
            title = {
                Text(stringResource(R.string.label))
            },
            text = {
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    label = {
                        Text(stringResource(R.string.label))
                    }
                )
            }
        )
    }
    if (showRingtoneEditor) {
        RingtonePickerDialog(
            onDismissRequest = {
                showRingtoneEditor = false
            },
            bottomContent = {
                Row(
                    modifier = Modifier.align(Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = obj.vibrate,
                        onCheckedChange = {
                            timerModel.updateVibrate(obj.id, it)
                        }
                    )
                    Text(text = stringResource(R.string.vibrate))
                }
            }
        ) { _, uri ->
            timerModel.updateRingtone(obj.id, uri)
        }
    }
}
