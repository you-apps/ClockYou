package com.bnyro.clock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R

@Composable
fun RgbColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var red by remember { mutableFloatStateOf(android.graphics.Color.red(initialColor).toFloat()) }
    var green by remember { mutableFloatStateOf(android.graphics.Color.green(initialColor).toFloat()) }
    var blue by remember { mutableFloatStateOf(android.graphics.Color.blue(initialColor).toFloat()) }

    val currentColor = Color(red.toInt(), green.toInt(), blue.toInt())

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.custom_rgb_color)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )

                Text(
                    text = String.format("#%06X", (currentColor.toArgb() and 0xFFFFFF)),
                    style = MaterialTheme.typography.labelLarge
                )

                ColorChannelSlider(stringResource(R.string.red), red, Color.Red) { red = it }
                ColorChannelSlider(stringResource(R.string.green), green, Color.Green) { green = it }
                ColorChannelSlider(stringResource(R.string.blue), blue, Color.Blue) { blue = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorSelected(currentColor.toArgb())
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ColorChannelSlider(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value.toInt()}", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color.copy(alpha = 0.5f)
            )
        )
    }
}