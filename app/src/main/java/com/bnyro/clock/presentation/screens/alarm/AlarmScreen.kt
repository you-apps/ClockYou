package com.bnyro.clock.presentation.screens.alarm

import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bnyro.clock.BuildConfig
import com.bnyro.clock.R
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.components.BlobIconBox
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.screens.alarm.components.AlarmFilterSection
import com.bnyro.clock.presentation.screens.alarm.components.AlarmItem
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.util.AlarmHelper

@Composable
fun AlarmScreen(
    onClickSettings: () -> Unit,
    onAlarm: (alarmId: Long) -> Unit,
    alarmModel: AlarmModel
) {
    val context = LocalContext.current
    val alarms by alarmModel.alarms.collectAsState()
    val filters by alarmModel.filters.collectAsState()

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            if (!AlarmHelper.hasPermission(context)) {
                val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${BuildConfig.APPLICATION_ID}".toUri()
                }
                context.startActivity(intent)
            }
        }
    }

    TopBarScaffold(title = stringResource(R.string.alarm), onClickSettings, fab = {
        if (!alarmModel.showFilter) {
            FloatingActionButton(
                onClick = {
                    onAlarm.invoke(0L)
                }
            ) {
                Icon(Icons.Rounded.Add, null)
            }
        }
    }, actions = {
        ClickableIcon(
            imageVector = Icons.Default.FilterAlt
        ) {
            alarmModel.showFilter = !alarmModel.showFilter
            if (!alarmModel.showFilter) alarmModel.resetFilters()
        }
    }) { pv ->

        if (alarms.isEmpty()) {
            BlobIconBox(icon = R.drawable.ic_alarm)
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pv)
        ) {

            item {
                if (alarmModel.showFilter) {
                    AlarmFilterSection(
                        filters,
                        { alarmModel.updateLabelFilter(it) },
                        { alarmModel.updateWeekDayFilter(it) },
                        { alarmModel.updateStartTimeFilter(it) },
                        { alarmModel.updateEndTimeFilter(it) },
                    )
                }

            }

            items(items = alarms, key = { it.id }) {
                AlarmItem(it, onClick = { alarm ->
                    onAlarm.invoke(alarm.id)
                }, onDeleteAlarm = { alarm ->
                    alarmModel.deleteAlarm(alarm)
                }, onUpdateAlarm = { alarm ->
                    alarmModel.updateAlarm(alarm)
                    if (alarm.enabled) {
                        alarmModel.createToast(alarm, context)
                    }
                })
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
