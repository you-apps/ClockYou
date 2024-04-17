package com.bnyro.clock.presentation.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.presentation.components.SwitchItem
import com.bnyro.clock.presentation.components.SwitchWithDivider
import com.bnyro.clock.presentation.screens.clock.components.TimeZonePickerDialog
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.DigitalClockWidgetOptions
import com.bnyro.clock.util.ThemeUtil
import com.bnyro.clock.util.loadDigitalClockWidgetSettings
import com.bnyro.clock.util.saveDigitalClockWidgetSettings
import com.bnyro.clock.util.updateDigitalClockWidget


class DigitalClockWidgetConfig : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )?.let {
            appWidgetId = it
        }
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        // get settings

        val options = loadDigitalClockWidgetSettings(appWidgetId)
        enableEdgeToEdge()
        setContent {
            DigitalClockWidgetSettings(
                options = options, onComplete = this::complete
            )
        }


    }

    private fun complete(options: DigitalClockWidgetOptions) {
        saveDigitalClockWidgetSettings(appWidgetId, options)
        updateDigitalClockWidget(appWidgetId, options)
        // return the result
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun DigitalClockWidgetSettings(
    options: DigitalClockWidgetOptions,
    onComplete: (DigitalClockWidgetOptions) -> Unit
) {
    val settingsModel: SettingsModel = viewModel()
    val clockModel: ClockModel = viewModel(factory = ClockModel.Factory)
    var showTimeZoneDialog by remember { mutableStateOf(false) }

    var customTimeZone by remember { mutableStateOf(options.timeZone) }
    var customTimeZoneName by remember { mutableStateOf(options.timeZoneName) }

    ClockYouTheme(
        darkTheme = true,
        customColorScheme = ThemeUtil.getSchemeFromSeed(
            settingsModel.customColor,
            true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var showDateOption by remember { mutableStateOf(options.showDate) }
            var showTimeOption by remember { mutableStateOf(options.showTime) }
            var showBackgroundOption by remember { mutableStateOf(options.showBackground) }
            var selectedDateSize by remember { mutableStateOf(options.dateTextSize) }
            var selectedTimeSize by remember { mutableStateOf(options.timeTextSize) }
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    SwitchItem(
                        title = stringResource(R.string.show_date),
                        isChecked = showDateOption,
                        icon = Icons.Rounded.CalendarToday
                    ) {
                        showDateOption = it
                    }
                    SwitchItem(
                        title = stringResource(R.string.show_time),
                        isChecked = showTimeOption,
                        icon = Icons.Rounded.CalendarToday
                    ) {
                        showTimeOption = it
                    }
                    SwitchItem(
                        title = stringResource(R.string.show_widget_background),
                        isChecked = showBackgroundOption,
                        icon = Icons.Rounded.CalendarToday
                    ) {
                        showBackgroundOption = it
                    }
                    TextSizeSelectSetting(
                        sizeOptions = DigitalClockWidgetOptions.dateSizeOptions,
                        title = stringResource(R.string.date_text_size),
                        currentSize = selectedDateSize
                    ) {
                        selectedDateSize = it
                    }
                    TextSizeSelectSetting(
                        sizeOptions = DigitalClockWidgetOptions.timeSizeOptions,
                        title = stringResource(R.string.time_text_size),
                        currentSize = selectedTimeSize
                    ) {
                        selectedTimeSize = it
                    }
                    SwitchWithDivider(
                        title = stringResource(R.string.timezone),
                        description = stringResource(R.string.use_a_different_time_zone_for_the_widget),
                        icon = Icons.Rounded.Language,
                        isChecked = customTimeZone != null,
                        onChecked = {
                            if (it) {
                                showTimeZoneDialog = true
                            } else {
                                customTimeZone = null
                            }
                        },
                        onClick = {
                            showTimeZoneDialog = true
                        }
                    )
                }
                Button(
                    modifier = Modifier.padding(bottom = 16.dp),
                    onClick = {
                        options.apply {
                            showDate = showDateOption
                            showTime = showTimeOption
                            dateTextSize = selectedDateSize
                            timeTextSize = selectedTimeSize
                            timeZone = customTimeZone
                            timeZoneName = customTimeZoneName
                            showBackground = showBackgroundOption
                        }
                        onComplete.invoke(options)
                    }) {
                    Text(stringResource(R.string.save))
                }
            }

        }
    }
    if (showTimeZoneDialog) {
        TimeZonePickerDialog(
            clockModel = clockModel,
            onDismissRequest = { showTimeZoneDialog = false }) { timeZone ->
            customTimeZone = timeZone.zoneId
            customTimeZoneName = timeZone.countryName
            showTimeZoneDialog = false
        }
    }
}

@Composable
fun TextSizeSelectSetting(
    sizeOptions: List<Float>,
    title: String,
    currentSize: Float,
    onSizeSelected: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.clickable(
            onClick = { expanded = true }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.FormatSize,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Row(
                Modifier
                    .clickable(
                        onClick = { expanded = true },
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = String.format("%.0f sp", currentSize),
                    style = MaterialTheme.typography.titleLarge
                )
                Icon(imageVector = Icons.Rounded.ExpandMore, contentDescription = null)

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sizeOptions.forEach { size ->
                        DropdownMenuItem(
                            onClick = {
                                onSizeSelected(size)
                                expanded = false
                            }, text = {
                                Text(
                                    text = String.format("%.0f sp", size),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            })
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DigitalClockWidgetSettings(
        options = DigitalClockWidgetOptions(),
        onComplete = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TextSizeSelectSettingPreview() {
    TextSizeSelectSetting(
        sizeOptions = DigitalClockWidgetOptions.dateSizeOptions,
        title = "Date text size",
        currentSize = DigitalClockWidgetOptions.DEFAULT_DATE_TEXT_SIZE,
        onSizeSelected = {}
    )
}