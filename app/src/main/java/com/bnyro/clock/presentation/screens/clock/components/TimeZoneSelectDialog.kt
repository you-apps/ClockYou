package com.bnyro.clock.presentation.screens.clock.components

import android.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.clock.model.ClockModel

@Composable
fun TimeZoneSelectDialog(
    clockModel: ClockModel, onDismissRequest: () -> Unit
) {
    val selectedTimeZones by clockModel.selectedTimeZones.collectAsState()
    val newTimeZones = remember {
        selectedTimeZones.toMutableStateList()
    }

    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        DialogButton(label = R.string.ok) {
            clockModel.setTimeZones(newTimeZones)
            onDismissRequest.invoke()
        }
    }, dismissButton = {
        DialogButton(label = R.string.cancel) {
            onDismissRequest.invoke()
        }
    }, title = {
        Text(stringResource(com.bnyro.clock.R.string.timezones))
    }, text = {
        var searchQuery by remember {
            mutableStateOf("")
        }

        Column(
            modifier = Modifier
                .heightIn(300.dp, 450.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(modifier = Modifier.padding(vertical = 10.dp),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(com.bnyro.clock.R.string.search)) })

            LazyColumn {
                val lowerQuery = searchQuery.lowercase()
                val filteredZones = clockModel.timeZones.filter {
                    it.countryName.lowercase().contains(lowerQuery) || it.zoneName.lowercase()
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
                            Text(text = it.zoneName)
                        }, supportingContent = {
                            Text(
                                text = it.countryName
                            )
                        }, leadingContent = {
                            Checkbox(checked = newTimeZones.contains(it),
                                onCheckedChange = { newCheckedState ->
                                    if (!newCheckedState) {
                                        newTimeZones.remove(it)
                                    } else {
                                        newTimeZones.add(it)
                                    }
                                })
                        }, trailingContent = {
                            Text((it.offset.toFloat() / 1000 / 3600).toString())
                        })
                    }
                }
            }
        }
    })
}