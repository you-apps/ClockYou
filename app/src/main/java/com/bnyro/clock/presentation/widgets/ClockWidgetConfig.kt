package com.bnyro.clock.presentation.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.presentation.components.SwitchItem
import com.bnyro.clock.presentation.components.SwitchWithDivider
import com.bnyro.clock.presentation.screens.clock.components.TimeZonePickerDialog
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.ThemeUtil
import com.bnyro.clock.util.widgets.TextColor
import com.bnyro.clock.util.widgets.getColorValue
import com.bnyro.clock.util.widgets.loadClockWidgetSettings
import com.bnyro.clock.util.widgets.saveClockWidgetSettings


abstract class ClockWidgetConfig : ComponentActivity() {

    abstract val defaultOptions: ClockWidgetOptions

    @get:LayoutRes
    abstract val widgetLayoutResource: Int

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
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

        val options = loadClockWidgetSettings(appWidgetId, defaultOptions)
        enableEdgeToEdge()
        setContent {
            val settingsModel: SettingsModel = viewModel()
            val darkTheme = when (settingsModel.themeMode) {
                SettingsModel.Theme.SYSTEM -> isSystemInDarkTheme()
                SettingsModel.Theme.DARK, SettingsModel.Theme.AMOLED -> true
                else -> false
            }
            ClockYouTheme(
                darkTheme = darkTheme,
                customColorScheme = ThemeUtil.getSchemeFromSeed(
                    settingsModel.customColor,
                    darkTheme
                )
            ) {
                val context = LocalContext.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {
                        CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.digital_clock_widget)) })
                    }) { pV ->
                        DigitalClockWidgetSettings(
                            modifier = Modifier.padding(pV),
                            options = options
                        ) {
                            complete(context, options)
                        }
                    }
                }
            }
        }
    }

    private fun complete(context: Context, options: ClockWidgetOptions) {
        saveClockWidgetSettings(appWidgetId, options)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val views = RemoteViews(packageName, widgetLayoutResource)
        updateClockWidget(context, views, options)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // return the result
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    abstract fun updateClockWidget(
        context: Context,
        views: RemoteViews,
        options: ClockWidgetOptions
    )
}

@Composable
fun DigitalClockWidgetSettings(
    modifier: Modifier = Modifier,
    options: ClockWidgetOptions,
    onComplete: (ClockWidgetOptions) -> Unit
) {

    val clockModel: ClockModel = viewModel()
    var showTimeZoneDialog by remember { mutableStateOf(false) }

    var customTimeZone by remember { mutableStateOf(options.timeZone) }
    var customTimeZoneName by remember { mutableStateOf(options.timeZoneName) }


    var showDateOption by remember { mutableStateOf(options.showDate) }
    var showTimeOption by remember { mutableStateOf(options.showTime) }
    var showBackgroundOption by remember { mutableStateOf(options.showBackground) }
    var selectedDateSize by remember { mutableFloatStateOf(options.dateTextSize) }
    var selectedTimeSize by remember { mutableFloatStateOf(options.timeTextSize) }
    var selectedTimeColor by remember { mutableStateOf(options.timeColor) }
    var selectedDateColor by remember { mutableStateOf(options.dateColor) }

    Column(
        modifier = modifier.padding(8.dp),
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
                sizeOptions = ClockWidgetOptions.dateSizeOptions,
                title = stringResource(R.string.date_text_size),
                currentSize = selectedDateSize
            ) {
                selectedDateSize = it
            }
            TextSizeSelectSetting(
                sizeOptions = ClockWidgetOptions.timeSizeOptions,
                title = stringResource(R.string.time_text_size),
                currentSize = selectedTimeSize
            ) {
                selectedTimeSize = it
            }
            ColorSelectSetting(
                label = stringResource(R.string.date_text_color),
                availableColors = ClockWidgetOptions.textColorOptions,
                currentColor = selectedDateColor
            ) {
                selectedDateColor = it
            }
            ColorSelectSetting(
                label = stringResource(R.string.time_text_color),
                availableColors = ClockWidgetOptions.textColorOptions,
                currentColor = selectedTimeColor
            ) {
                selectedTimeColor = it
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
                    dateColor = selectedDateColor
                    timeColor = selectedTimeColor
                    timeZone = customTimeZone
                    timeZoneName = customTimeZoneName
                    showBackground = showBackgroundOption
                }
                onComplete.invoke(options)
            }) {
            Text(stringResource(R.string.save))
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

@Composable
fun ColorSelectSetting(
    label: String,
    availableColors: List<TextColor>,
    currentColor: TextColor,
    onColorSelected: (TextColor) -> Unit
) {
    val context = LocalContext.current

    Column(
        Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(availableColors) { textColor ->
                val colorValue = Color(textColor.getColorValue(context))

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(CircleShape)
                        .size(36.dp)
                        .background(colorValue)
                        .clickable {
                            onColorSelected(textColor)
                        }
                ) {
                    if (currentColor == textColor) {
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.contentColorFor(colorValue)
                        )
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
        options = ClockWidgetOptions(
            dateTextSize = 16f,
            timeTextSize = 52f
        ),
        onComplete = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TextSizeSelectSettingPreview() {
    TextSizeSelectSetting(
        sizeOptions = ClockWidgetOptions.dateSizeOptions,
        title = "Date text size",
        currentSize = 16f,
        onSizeSelected = {}
    )
}