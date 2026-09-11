package com.bnyro.clock.presentation.screens.timer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.clock.domain.model.TimerSettings
import com.bnyro.clock.util.extensions.addZero

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedTimerItem(
    timer: TimerSettings,
    isSelected: Boolean,
    onStart: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = timer.seconds / 3600
    val minutes = timer.seconds % 3600 / 60
    val seconds = timer.seconds % 60
    val cardShape = RoundedCornerShape(20.dp)

    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(cardShape)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = cardShape
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = timer.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "$hours:${minutes.addZero()}:${seconds.addZero()}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible
                )
            }

            FilledIconButton(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(48.dp),
                onClick = onStart
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            }
        }
    }
}
