package com.bnyro.clock.presentation.screens.alarm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.components.DialogButtonStyle
import com.bnyro.clock.util.AlarmHelper
import com.bnyro.clock.util.TimeHelper
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AlarmCard(
    alarm: Alarm,
    onClick: () -> Unit,
    isAlarmEnabled: Boolean,
    onEnable: (Boolean) -> Unit,
    canDismiss: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val millisRemaining = AlarmHelper.getAlarmTime(alarm) - System.currentTimeMillis()
                alarm.label?.let {
                    Row(
                        modifier = Modifier
                            .padding(start = 5.dp, end = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Label, null)
                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = it,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = TimeHelper.millisToFormatted(context, alarm.time),
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp
                )
                Text(
                    text = if (millisRemaining <= 0) {
                        stringResource(R.string.alarm_starting_now)
                    } else {
                        stringResource(
                            R.string.alarm_starts_in,
                            TimeHelper.durationToFormatted(context, millisRemaining.milliseconds)
                        )
                    }
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (canDismiss) {
                    DialogButton(R.string.dismiss, DialogButtonStyle.SECONDARY, onDismiss)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.padding(horizontal = 8.dp)) {
                        when {
                            alarm.isOneTime -> {
                                Text(text = stringResource(R.string.one_time))
                            }

                            alarm.repeatUnit != RepeatUnit.WEEK || alarm.repeatInterval > 1 -> {
                                Text(
                                    text = pluralStringResource(
                                        id = alarm.repeatUnit.summary,
                                        count = alarm.repeatInterval,
                                        alarm.repeatInterval
                                    )
                                )
                            }

                            alarm.isRepeatEveryday -> {
                                Text(text = stringResource(R.string.repeating))
                            }

                            alarm.isWeekends -> {
                                Text(text = stringResource(R.string.weekends))
                            }

                            alarm.isWeekdays -> {
                                Text(text = stringResource(R.string.weekdays))
                            }

                            else -> {
                                val daysOfWeek = remember {
                                    AlarmHelper.getDaysOfWeekByLocale(context)
                                }
                                daysOfWeek.forEach { (day, index) ->
                                    val enabled = alarm.days.contains(index)
                                    Text(
                                        modifier = Modifier.padding(horizontal = 2.dp),
                                        text = day,
                                        color = if (enabled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.5f
                                            )
                                        },
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    Switch(
                        checked = isAlarmEnabled,
                        onCheckedChange = onEnable
                    )
                }
            }
        }
    }
}
