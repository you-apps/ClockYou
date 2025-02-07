package com.bnyro.clock.presentation.screens.clock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.TimeZone
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.util.TimeHelper

@Composable
fun TimeZoneSelectDialog(
    clockModel: ClockModel, onDismissRequest: () -> Unit
) {
    val selectedTimeZones by clockModel.selectedTimeZones.collectAsState()
    val newTimeZones = remember {
        selectedTimeZones.toMutableStateList()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = remember { DialogProperties(usePlatformDefaultWidth = false) }) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                var filteredZones by remember { mutableStateOf(clockModel.timeZones) }
                var searchQuery by remember {
                    mutableStateOf("")
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        val lowerQuery = searchQuery.lowercase()
                        filteredZones = clockModel.timeZones.filter {
                            it.countryName.lowercase()
                                .contains(lowerQuery) || it.zoneName.lowercase()
                                .contains(lowerQuery)
                        }
                    },
                    placeholder = { Text(stringResource(R.string.search_country_timezone)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(50),
                    leadingIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = null
                            )
                        }
                    }
                )

                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    items(filteredZones) {
                        TimeZoneCard(
                            it,
                            selected = newTimeZones.contains(it),
                            onClick = { newCheckedState ->
                                if (!newCheckedState) {
                                    newTimeZones.remove(it)
                                } else {
                                    newTimeZones.add(it)
                                }
                            })
                    }
                }
                Row(
                    Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = { onDismissRequest.invoke() }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                    Button(onClick = {
                        clockModel.setTimeZones(newTimeZones)
                        onDismissRequest.invoke()
                    }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
fun TimeZonePickerDialog(
    clockModel: ClockModel, onDismissRequest: () -> Unit, onSelectTimeZone: (TimeZone) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = remember { DialogProperties(usePlatformDefaultWidth = false) }) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                var filteredZones by remember { mutableStateOf(clockModel.timeZones) }
                var searchQuery by remember {
                    mutableStateOf("")
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        val lowerQuery = searchQuery.lowercase()
                        filteredZones = clockModel.timeZones.filter {
                            it.countryName.lowercase()
                                .contains(lowerQuery) || it.zoneName.lowercase()
                                .contains(lowerQuery)
                        }
                    },
                    placeholder = { Text(stringResource(R.string.search_country_timezone)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(50),
                    leadingIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = null
                            )
                        }
                    }
                )

                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    items(filteredZones) {
                        TimeZoneCard(
                            it,
                            allowSelection = false,
                            onClick = { _ ->
                                onSelectTimeZone.invoke(it)
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeZoneCard(
    timeZone: TimeZone,
    allowSelection: Boolean = true,
    selected: Boolean = false,
    onClick: (Boolean) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .clickable {
                onClick.invoke(!selected)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (allowSelection) {
                IconButton(onClick = {
                    onClick.invoke(!selected)
                }) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = timeZone.zoneName,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = timeZone.countryName,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                modifier = Modifier.padding(
                    horizontal = 16.dp, vertical = 8.dp
                ),
                text = TimeHelper.formatGMTTimeDifference(timeZone.zoneId),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
@Preview
private fun TimeZoneCardPreview() {
    TimeZoneCard(timeZone = TimeZone(
        key = "America/New_York,New_York,United States",
        zoneName = "New_York",
        countryName = "United States",
        zoneId = "America/New_York"
    ), selected = true, onClick = {})
}