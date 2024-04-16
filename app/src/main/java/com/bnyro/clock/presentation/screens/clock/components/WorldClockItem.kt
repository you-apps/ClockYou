package com.bnyro.clock.presentation.screens.clock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.TimeZone
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.clock.model.ClockModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WorldClockItem(
    clockModel: ClockModel, timeZone: TimeZone
) {
    var showDeletionDialog by remember {
        mutableStateOf(false)
    }

    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { dismissValue ->
        when (dismissValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                showDeletionDialog = true
            }

            else -> {}
        }
        false
    })
    SwipeToDismissBox(state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        content = {
            WorldClockCard(clockModel, timeZone)
        },
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        })

    if (showDeletionDialog) {
        AlertDialog(onDismissRequest = { showDeletionDialog = false }, title = {
            Text(text = stringResource(R.string.delete_world_clock))
        }, text = {
            Text(text = stringResource(R.string.irreversible))
        }, confirmButton = {
            DialogButton(label = android.R.string.ok) {
                clockModel.deleteTimeZone(timeZone)
                showDeletionDialog = false
            }
        }, dismissButton = {
            DialogButton(label = android.R.string.cancel) {
                showDeletionDialog = false
            }
        })
    }
}