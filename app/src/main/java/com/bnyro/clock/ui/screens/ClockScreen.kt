package com.bnyro.clock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.obj.SortOrder
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.model.ClockModel
import com.bnyro.clock.util.TimeHelper

@Composable
fun ClockScreen(clockModel: ClockModel) {
    var showTimeZoneDialog by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        clockModel.startLifecycle()
        onDispose {
            clockModel.stopLifecycle()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
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
                    val dateTime = clockModel.getDateWithOffset(
                        clockModel.currentDate,
                        timeZone.offset
                    )
                    val (date, time) = TimeHelper.formatDateTime(dateTime)

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
                            Text(
                                text = time,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = date,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(10.dp))
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                showTimeZoneDialog = true
            }
        ) {
            Icon(Icons.Default.Create, null)
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

                Column {
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
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var checked by remember {
                                    mutableStateOf(newTimeZones.contains(it))
                                }
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { newState ->
                                        checked = newState
                                        if (!checked) {
                                            newTimeZones.remove(it)
                                        } else {
                                            newTimeZones.add(
                                                it
                                            )
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
