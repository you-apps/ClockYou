package com.bnyro.clock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.extensions.addZero
import com.bnyro.clock.obj.ScheduledObject
import com.bnyro.clock.obj.WatchState
import com.bnyro.clock.ui.dialog.RingtonePickerDialog
import com.bnyro.clock.ui.model.TimerModel

@Composable
fun TimerItem(obj: ScheduledObject, index: Int, timerModel: TimerModel) {
    val context = LocalContext.current
    val minutes = obj.currentPosition.value / 60000
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
                )
            ) {
                Column {
                    obj.label.value?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$minutes:${seconds.addZero()}",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                ClickableIcon(imageVector = Icons.Default.Edit) {
                    showLabelEditor = true
                }
                ClickableIcon(imageVector = Icons.Default.Notifications) {
                    showRingtoneEditor = true
                }
                ClickableIcon(imageVector = Icons.Default.Close) {
                    timerModel.stopTimer(context, index)
                }
                FilledIconButton(
                    onClick = {
                        when (obj.state.value) {
                            WatchState.PAUSED -> timerModel.resumeTimer(index)
                            WatchState.RUNNING -> timerModel.pauseTimer(index)
                            else -> timerModel.startTimer(context)
                        }
                    }
                ){
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
                modifier = Modifier.fillMaxWidth()
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
                    timerModel.service?.updateLabel(obj.id, newLabel)
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
        RingtonePickerDialog(onDismissRequest = {
            showRingtoneEditor = false
        }) { _, uri ->
            timerModel.service?.updateRingtone(obj.id, uri)
        }
    }
}
