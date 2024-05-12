package com.bnyro.clock.presentation.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bnyro.clock.util.Preferences

@Composable
fun SwitchPref(
    prefKey: String,
    title: String,
    summary: String? = null,
    defaultValue: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    var checked by remember {
        mutableStateOf(
            Preferences.instance.getBoolean(prefKey, defaultValue)
        )
    }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                checked = !checked
                Preferences.edit { putBoolean(prefKey, checked) }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreferenceItem(
            modifier = Modifier.weight(1f),
            title = title,
            summary = summary
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                Preferences.edit { putBoolean(prefKey, it) }
                onCheckedChange.invoke(it)
            }
        )
    }
}
