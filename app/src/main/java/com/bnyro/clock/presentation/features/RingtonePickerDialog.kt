package com.bnyro.clock.presentation.features

import android.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.screens.timer.model.RingingToneModel
import com.bnyro.clock.util.PickPersistentFileContract
import com.bnyro.clock.util.extensions.getContentFileName

@Composable
fun RingtonePickerDialog(
    onDismissRequest: () -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit = {},
    onSelection: (String, Uri) -> Unit
) {
    val context = LocalContext.current
    val ringingToneModel: RingingToneModel = viewModel()

    val pickSoundFile = rememberLauncherForActivityResult(PickPersistentFileContract()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        onSelection.invoke(context.getContentFileName(uri).orEmpty(), uri)
        onDismissRequest.invoke()
    }

    DisposableEffect(Unit) {
        onDispose {
            ringingToneModel.stopRinging()
        }
    }
    AlertDialog(
        onDismissRequest = {
            ringingToneModel.stopRinging()
            onDismissRequest.invoke()
        },
        confirmButton = {
            DialogButton(R.string.cancel) {
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(400.dp, 500.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (ringingToneModel.sounds.isEmpty()) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(ringingToneModel.sounds) { (title, uri) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelection.invoke(title, uri)
                                        onDismissRequest.invoke()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 2.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    bottomContent()
                }
            }
        }
    )
}
