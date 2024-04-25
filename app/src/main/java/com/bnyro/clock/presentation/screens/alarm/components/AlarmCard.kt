package com.bnyro.clock.presentation.screens.alarm.components

import android.text.format.DateUtils
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
import androidx.compose.material.icons.filled.Label
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.util.AlarmHelper

@Composable
fun AlarmCard(
    alarm: Alarm,
    onClick: () -> Unit,
    isAlarmEnabled: Boolean,
    onEnable: (Boolean) -> Unit
) {
    val context = LocalContext.current
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                val relativeTimeString = DateUtils.getRelativeTimeSpanString(
                    AlarmHelper.getAlarmTime(alarm),
                )
                alarm.label?.let {
                    Row(
                        modifier = Modifier
                            .padding(start = 5.dp, end = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Label, null)
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
                    text = alarm.formattedTime,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp
                )
                Text(
                    modifier = Modifier.padding(start = 6.dp),
                    text = "$relativeTimeString."
                )
            }

            Row(Modifier.padding(horizontal = 8.dp)) {
                when {
                    !alarm.repeat -> {
                        Text(text = stringResource(R.string.one_time))
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
                                text = day,
                                color = if (enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f
                                    )
                                },
                                fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal
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
