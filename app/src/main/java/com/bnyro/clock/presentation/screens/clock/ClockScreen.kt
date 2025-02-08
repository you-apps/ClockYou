package com.bnyro.clock.presentation.screens.clock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.TimeZoneSortOrder
import com.bnyro.clock.navigation.TopBarScaffold
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.screens.clock.components.DigitalClockDisplay
import com.bnyro.clock.presentation.screens.clock.components.TimeZoneSelectDialog
import com.bnyro.clock.presentation.screens.clock.components.WorldClockItem
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.util.Preferences

@Composable
fun ClockScreen(
    onClickSettings: () -> Unit, clockModel: ClockModel
) {
    var showTimeZoneDialog by remember {
        mutableStateOf(false)
    }

    TopBarScaffold(title = stringResource(R.string.clock), onClickSettings, actions = {
        TopBarActions(clockModel)
    }, fab = {
        FloatingActionButton(onClick = {
            showTimeZoneDialog = true
        }) {
            Icon(Icons.Rounded.Add, null)
        }
    }) { pv ->

        val selectedZones by clockModel.selectedTimeZones.collectAsState()
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .padding(pv)
        ) {
            item {
                DigitalClockDisplay()
            }
            items(items = selectedZones, key = { it.key }) { timeZone ->
                WorldClockItem(clockModel, timeZone)
            }
        }
    }

    if (showTimeZoneDialog) {
        TimeZoneSelectDialog(clockModel, onDismissRequest = {
            showTimeZoneDialog = false
        })
    }
}

@Composable
private fun TopBarActions(clockModel: ClockModel) {
    Box {
        var showDropdown by remember {
            mutableStateOf(false)
        }

        ClickableIcon(imageVector = Icons.Default.Sort) {
            showDropdown = true
        }

        DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
            TimeZoneSortOrder.entries.forEach {
                DropdownMenuItem(text = {
                    Text(stringResource(it.value))
                }, onClick = {
                    clockModel.updateSortOrder(it)
                    Preferences.edit {
                        putString(Preferences.clockSortOrder, it.name)
                    }
                    showDropdown = false
                })
            }
        }
    }
}