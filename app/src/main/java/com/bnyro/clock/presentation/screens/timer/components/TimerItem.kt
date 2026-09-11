package com.bnyro.clock.presentation.screens.timer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.clock.domain.model.TimerObject
import com.bnyro.clock.domain.model.WatchState
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.screens.timer.model.TimerModel
import com.bnyro.clock.util.TimeHelper
import com.bnyro.clock.util.extensions.addZero
import java.time.ZonedDateTime

@Composable
fun TimerItem(
    obj: TimerObject,
    timerModel: TimerModel,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFinished = obj.currentPosition.value <= 0
    val hours = obj.secondsLeft / 3600
    val minutes = (obj.secondsLeft % 3600) / 60
    val seconds = obj.secondsLeft % 60
    val cardShape = RoundedCornerShape(20.dp)

    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(cardShape)
            .clickable(onClick = onEdit),
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mutedContentColor = MaterialTheme.colorScheme.onSurfaceVariant

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = null,
                            tint = mutedContentColor
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = obj.label.value,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal,
                            color = mutedContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = if (isFinished) "0:00:00" else "$hours:${minutes.addZero()}:${seconds.addZero()}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = if (isFinished) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible
                    )

                    if (!isFinished) {
                        AnimatedVisibility(obj.state.value == WatchState.RUNNING) {
                            Row(
                                modifier = Modifier
                                    .offset(x = (-6).dp, y = (2.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = mutedContentColor
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = TimeHelper.formatTime(
                                        context,
                                        ZonedDateTime.now().plusHours(hours.toLong())
                                            .plusMinutes(minutes.toLong()).plusSeconds(seconds.toLong())
                                    ),
                                    color = mutedContentColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                if (isFinished) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = { timerModel.restartTimer(context, obj.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        FilledIconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = { timerModel.stopTimer(context, obj.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                } else {
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy((-10).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClickableIcon(imageVector = Icons.Default.MoreTime) {
                            timerModel.addTimeToTimer(context, obj.id)
                        }

                        ClickableIcon(imageVector = Icons.Default.Refresh) {
                            timerModel.restartTimer(context, obj.id)
                        }

                        ClickableIcon(imageVector = Icons.Default.Close) {
                            timerModel.stopTimer(context, obj.id)
                        }
                    }
                }
                if (!isFinished) {
                    FilledIconButton(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(48.dp),
                        onClick = { timerModel.pauseResumeTimer(context, obj.id) }
                    ) {
                        Icon(
                            imageVector = if (obj.state.value == WatchState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                }
            }
            if (!isFinished) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .height(8.dp),
                    progress = { obj.currentPosition.value / obj.initialPosition.value.toFloat() },
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
