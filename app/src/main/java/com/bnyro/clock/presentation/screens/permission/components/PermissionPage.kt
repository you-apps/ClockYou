package com.bnyro.clock.presentation.screens.permission.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bnyro.clock.R
import com.bnyro.clock.presentation.components.BlobIconBox

@Composable
fun PermissionRequestPage(
    title: String,
    subtitle: String,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit,
    @DrawableRes icon: Int,
) {
    Column(
        Modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            BlobIconBox(icon)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onClickConfirm,
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.allow),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(
                onClick = onClickCancel, colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = stringResource(R.string.maybe_later),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionRequestPagePreview() {
    PermissionRequestPage(
        title = "Enable Alarm Permissions",
        subtitle = "Alarm Permissions are required to schedule alarms",
        onClickConfirm = {},
        onClickCancel = {},
        icon = R.drawable.ic_alarm
    )
}
