package com.bnyro.clock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.ui.theme.LabelColor

@Composable
fun LabelColorPreference(color: Int, onColorSelected: (Int) -> Unit) {
    var showPalette by rememberSaveable { mutableStateOf(false) }
    var showCustomColor by rememberSaveable { mutableStateOf(false) }
    var selectedColor by rememberSaveable(color) { mutableIntStateOf(color) }
    val preset = LabelColor.entries.firstOrNull { it.argb == color }

    ListItem(
        headlineContent = { Text(stringResource(R.string.label_color)) },
        supportingContent = { Text(stringResource(preset?.label ?: R.string.custom_color)) },
        leadingContent = {
            Box(Modifier.size(24.dp).background(Color(color), CircleShape))
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable {
            selectedColor = color
            showPalette = true
        }
    )

    if (showPalette) {
        AlertDialog(
            onDismissRequest = { showPalette = false },
            title = { Text(stringResource(R.string.select_label_color)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                    LabelColor.entries.forEach { option ->
                        val selected = option.argb == selectedColor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = selected, role = Role.RadioButton) {
                                    selectedColor = option.argb
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(24.dp).then(
                                    if (selected) Modifier.background(Color(option.argb), CircleShape)
                                    else Modifier.border(4.dp, Color(option.argb), CircleShape)
                                )
                            )
                            Text(
                                text = stringResource(option.label),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                    val customSelected = LabelColor.entries.none { it.argb == selectedColor }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = customSelected, role = Role.RadioButton) {
                                showCustomColor = true
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val customColor = if (customSelected) Color(selectedColor) else MaterialTheme.colorScheme.primary
                        Box(
                            Modifier.size(24.dp).then(
                                if (customSelected) Modifier.background(customColor, CircleShape)
                                else Modifier.border(4.dp, customColor, CircleShape)
                            )
                        )
                        Text(
                            text = stringResource(R.string.custom_color),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                DialogButton(R.string.save, DialogButtonStyle.PRIMARY) {
                    onColorSelected(selectedColor)
                    showPalette = false
                }
            },
            dismissButton = {
                DialogButton(android.R.string.cancel, DialogButtonStyle.SECONDARY) {
                    showPalette = false
                }
            }
        )
    }

    if (showCustomColor) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = { selectedColor = it },
            onDismissRequest = { showCustomColor = false }
        )
    }
}
