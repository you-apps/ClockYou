package com.bnyro.clock.ui.dialog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.extensions.getContentFileName
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.components.DialogButton
import com.bnyro.clock.ui.model.RingingToneModel
import com.bnyro.clock.util.PickPersistentFileContract

@Composable
fun RingtonePickerDialog(
    onDismissRequest: () -> Unit,
    onSelection: (String, Uri) -> Unit
) {
    val context = LocalContext.current
    val ringingToneModel: RingingToneModel = viewModel(factory = RingingToneModel.Factory)

    val pickSoundFile = rememberLauncherForActivityResult(PickPersistentFileContract()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        onSelection.invoke(context.getContentFileName(uri).orEmpty(), uri)
        onDismissRequest.invoke()
    }

    AlertDialog(
        onDismissRequest = {
            ringingToneModel.stopRinging()
            onDismissRequest.invoke()
        },
        confirmButton = {
            DialogButton(android.R.string.cancel) {
                ringingToneModel.stopRinging()
                onDismissRequest.invoke()
            }
        },
        dismissButton = {
            DialogButton(com.bnyro.clock.R.string.custom_file) {
                ringingToneModel.stopRinging()
                pickSoundFile.launch(arrayOf("audio/*"))
            }
        },
        title = {
            Text(stringResource(com.bnyro.clock.R.string.sound))
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(400.dp, 500.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (ringingToneModel.sounds.isEmpty()) {
                    CircularProgressIndicator()
                } else {
                    ringingToneModel.sounds.forEach { (title, uri) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelection.invoke(title, uri)
                                    onDismissRequest.invoke()
                                }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title)
                            Spacer(modifier = Modifier.weight(1f))
                            ClickableIcon(imageVector = Icons.Default.NotificationsActive) {
                                ringingToneModel.playRingingTone(context, uri)
                            }
                        }
                    }
                }
            }
        }
    )
}
