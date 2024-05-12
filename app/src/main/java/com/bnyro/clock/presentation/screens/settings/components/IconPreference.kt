package com.bnyro.clock.presentation.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bnyro.clock.presentation.components.ClickableIcon

@Composable
fun IconPreference(
    title: String,
    summary: String? = null,
    imageVector: ImageVector,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick.invoke()
            }
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreferenceItem(
            title = title,
            summary = summary
        )
        ClickableIcon(
            imageVector = imageVector,
            onClick = onClick
        )
    }
}
