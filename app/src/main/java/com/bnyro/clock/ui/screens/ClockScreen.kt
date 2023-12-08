package com.bnyro.clock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.obj.SortOrder
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.model.ClockModel
import com.bnyro.clock.ui.nav.TopBarScaffold
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper

@Composable
fun ClockScreen(
    onClickSettings: () -> Unit,
    clockModel: ClockModel
) {
    var showTimeZoneDialog by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        clockModel.startLifecycle()
        onDispose {
            clockModel.stopLifecycle()
        }
    }

    TopBarScaffold(title = stringResource(R.string.clock), onClickSettings, actions = {
        Box {
            var showDropdown by remember {
                mutableStateOf(false)
            }

            ClickableIcon(imageVector = Icons.Default.Sort) {
                showDropdown = true
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                SortOrder.values().forEach {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(it.value))
                        },
                        onClick = {
                            clockModel.sortOrder = it
                            Preferences.edit {
                                putString(Preferences.clockSortOrder, it.name)
                            }
                            showDropdown = false
                        }
                    )
                }
            }
        }
    }, fab = {
        FloatingActionButton(
            onClick = {
                showTimeZoneDialog = true
            }
        ) {
            Icon(Icons.Default.Create, null)
        }
    }) { pv ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .padding(pv)
                .verticalScroll(scrollState)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 28.dp)
                ) {
                    val (date, time) = TimeHelper.formatDateTime(clockModel.currentDate)
                    Text(
                        text = time,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val zones = clockModel.selectedTimeZones.distinct()
            val sortedZones = when (clockModel.sortOrder) {
                SortOrder.ALPHABETIC -> zones.sortedBy { it.displayName }
                SortOrder.OFFSET -> zones.sortedBy { it.offset }
            }
            sortedZones.forEach { timeZone ->
                // needed for auto updating the time displayed / re-composition
                val (date, time) = clockModel.currentDate.let {
                    clockModel.getDateWithOffset(timeZone.name)
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = timeZone.displayName.uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            val context = LocalContext.current
                            Text(
                                text = TimeHelper.formatHourDifference(context, timeZone),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showTimeZoneDialog) {
        val newTimeZones = remember {
            clockModel.selectedTimeZones.toMutableStateList()
        }

        AlertDialog(
            onDismissRequest = { showTimeZoneDialog = false },
            confirmButton = {
                DialogButton(label = android.R.string.ok) {
                    clockModel.setTimeZones(newTimeZones)
                    showTimeZoneDialog = false
                }
            },
            dismissButton = {
                DialogButton(label = android.R.string.cancel) {
                    showTimeZoneDialog = false
                }
            },
            title = {
                Text(stringResource(R.string.timezones))
            },
            text = {
                var searchQuery by remember {
                    mutableStateOf("")
                }

                Column(
                    modifier = Modifier
                        .heightIn(300.dp, 450.dp)
                        .fillMaxSize()
                ) {
                    OutlinedTextField(
                        modifier = Modifier.padding(vertical = 10.dp),
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.search)) }
                    )

                    LazyColumn {
                        val lowerQuery = searchQuery.lowercase()
                        val filteredZones = clockModel.timeZones.filter {
                            it.name.lowercase().contains(lowerQuery)
                        }

                        items(filteredZones) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = newTimeZones.contains(it),
                                    onCheckedChange = { newCheckedState ->
                                        if (!newCheckedState) {
                                            newTimeZones.remove(it)
                                        } else {
                                            newTimeZones.add(it)
                                        }
                                    }
                                )
                                Text(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp),
                                    text = it.displayName
                                )
                                Text((it.offset.toFloat() / 1000 / 3600).toString())
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        }
                    }
                }
            }
        )
    }
}
