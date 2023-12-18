package com.bnyro.clock.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.obj.SortOrder
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.model.ClockModel
import com.bnyro.clock.ui.nav.TopBarScaffold
import com.bnyro.clock.util.Preferences
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ClockScreen(
    onClickSettings: () -> Unit,
    clockModel: ClockModel
) {
    var showTimeZoneDialog by remember {
        mutableStateOf(false)
    }

    TopBarScaffold(title = stringResource(R.string.clock), onClickSettings, actions = {
        Box {
            var showDropdown by remember {
                mutableStateOf(false)
            }

            ClickableIcon(imageVector = Icons.Default.Sort) {
                showDropdown = true
            }

            var sortOrder by remember { mutableStateOf(clockModel.sortOrder) }

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
                            sortOrder = it
                            clockModel.updateSortOrder(it)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dateTime by produceState(
                    initialValue = TimeHelper.formatDateTime(TimeHelper.getTimeByZone()),
                    producer = {
                        while (isActive) {
                            value = TimeHelper.formatDateTime(TimeHelper.getTimeByZone())
                            delay(1000)
                        }
                    }
                )
                Text(
                    text = dateTime.second,
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = dateTime.first,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            clockModel.sortedZones.forEach { timeZone ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        val dateTime by produceState(
                            initialValue = clockModel.getDateWithOffset(timeZone.name)
                        ) {
                            while (isActive) {
                                value = clockModel.getDateWithOffset(timeZone.name)
                                delay(1000)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = timeZone.displayName,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = timeZone.countryName,
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    text = dateTime.second,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val context = LocalContext.current
                        Text(
                            text = TimeHelper.formatHourDifference(
                                context,
                                timeZone
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = dateTime.first,
                            style = MaterialTheme.typography.bodyMedium
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
                            it.countryName.lowercase()
                                .contains(lowerQuery) || it.displayName.lowercase()
                                .contains(lowerQuery)
                        }

                        items(filteredZones) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ListItem(headlineContent = {
                                    Text(text = it.displayName)
                                }, supportingContent = {
                                    Text(
                                        text = it.countryName
                                    )
                                }, leadingContent = {
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
                                }, trailingContent = {
                                    Text((it.offset.toFloat() / 1000 / 3600).toString())
                                })
                            }
                        }
                    }
                }
            }
        )
    }
}
