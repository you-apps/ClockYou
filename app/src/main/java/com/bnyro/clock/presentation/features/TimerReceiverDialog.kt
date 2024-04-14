package com.bnyro.clock.presentation.features

import android.text.format.DateUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.timer.model.TimerModel

@Composable
fun TimerReceiverDialog(duration: Int) {
    var showDialog by remember {
        mutableStateOf(true)
    }
    val timerModel: TimerModel = viewModel()
    val context = LocalContext.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.start_timer)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.duration_seconds,
                        DateUtils.formatElapsedTime(duration.toLong())
                    )
                )
            },
            confirmButton = {
                DialogButton(android.R.string.ok) {
                    timerModel.startTimer(context, duration)
                    showDialog = false
                }
            },
            dismissButton = {
                DialogButton(android.R.string.cancel) {
                    showDialog = false
                }
            }
        )
    }
}
