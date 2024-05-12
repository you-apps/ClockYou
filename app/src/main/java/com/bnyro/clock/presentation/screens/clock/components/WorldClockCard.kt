package com.bnyro.clock.presentation.screens.clock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.clock.domain.model.TimeZone
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.util.TimeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun WorldClockCard(
    clockModel: ClockModel, timeZone: TimeZone
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            val dateTime by produceState(
                initialValue = clockModel.getDateWithOffset(timeZone.zoneId)
            ) {
                while (isActive) {
                    value = clockModel.getDateWithOffset(timeZone.zoneId)
                    delay(1000)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = timeZone.zoneName,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = timeZone.countryName,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        )
                ) {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = 16.dp, vertical = 8.dp
                        ),
                        text = dateTime.second,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val context = LocalContext.current
            Text(
                text = TimeHelper.formatHourDifference(
                    context, timeZone
                ), style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = dateTime.first, style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}