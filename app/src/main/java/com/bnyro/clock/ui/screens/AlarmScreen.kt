package com.bnyro.clock.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.model.AlarmModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(alarmModel: AlarmModel) {
    var showCreationDialog by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column() {
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                showCreationDialog = true
            },
        ) {
            Icon(Icons.Default.Create, null)
        }
    }

    if (showCreationDialog) {
        val state = rememberTimePickerState()

        AlertDialog(
            onDismissRequest = { showCreationDialog = false },
            confirmButton = {
                DialogButton(label = android.R.string.ok) {
                    showCreationDialog = false
                }
            },
            dismissButton = {
                DialogButton(label = android.R.string.cancel) {
                    showCreationDialog = false
                }
            },
            title = {
                Text(stringResource(R.string.new_alarm))
            },
            text = {
                Spacer(modifier = Modifier.height(10.dp))
                TimePicker(state = state)
            },
        )
    }
}
