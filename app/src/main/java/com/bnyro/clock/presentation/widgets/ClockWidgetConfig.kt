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
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.domain.model.ShadowPreset
import com.bnyro.clock.presentation.components.DialogButton
import com.bnyro.clock.presentation.components.DialogButtonStyle
import com.bnyro.clock.presentation.components.RgbColorPickerDialog
import com.bnyro.clock.presentation.components.SwitchItem
import com.bnyro.clock.presentation.components.SwitchWithDivider
import com.bnyro.clock.presentation.screens.clock.components.TimeZonePickerDialog
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.components.SettingsCategory
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.presentation.screens.timer.components.ScrollTimePicker
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.ThemeUtil
import com.bnyro.clock.util.widgets.TextColor
import com.bnyro.clock.util.widgets.getColorValue
import com.bnyro.clock.util.widgets.hasClockWidgetSettings
import com.bnyro.clock.util.widgets.loadClockWidgetSettings
import com.bnyro.clock.util.widgets.saveClockWidgetSettings

private enum class ColorTarget { TIME, DATE }

abstract class ClockWidgetConfig : ComponentActivity() {

    abstract val defaultOptions: ClockWidgetOptions

    @get:StringRes
    abstract val titleResource: Int

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

        // get settings

        val options = loadClockWidgetSettings(appWidgetId, defaultOptions)
        if (hasClockWidgetSettings(appWidgetId)) {
            setResult(Activity.RESULT_CANCELED, resultValue)
        } else {
            applyToWidget(this, options)
            setResult(Activity.RESULT_OK, resultValue)
        }
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
                        CenterAlignedTopAppBar(title = { Text(text = stringResource(titleResource)) })
                    }) { pV ->
                        DigitalClockWidgetSettings(
                            modifier = Modifier.padding(pV),
                            options = options,
                            onCancel = { finish() }
                        ) { updatedOptions ->
                            complete(context, updatedOptions)
                        }
                    }
                }
            }
        }
    }

    private fun applyToWidget(context: Context, options: ClockWidgetOptions) {
        saveClockWidgetSettings(appWidgetId, options)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val views = RemoteViews(packageName, widgetLayoutResource)
        updateClockWidget(context, views, appWidgetId, options)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun complete(context: Context, options: ClockWidgetOptions) {
        applyToWidget(context, options)
        // return the result
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    abstract fun updateClockWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        options: ClockWidgetOptions
    )
}

@Composable
fun DigitalClockWidgetSettings(
    modifier: Modifier = Modifier,
    options: ClockWidgetOptions,
    onCancel: () -> Unit,
    onComplete: (ClockWidgetOptions) -> Unit
) {
    val clockModel: ClockModel = viewModel()
    var showTimeZoneDialog by remember { mutableStateOf(false) }

    var customTimeZone by remember { mutableStateOf(options.timeZone) }
    var customTimeZoneName by remember { mutableStateOf(options.timeZoneName) }

    var showDateOption by remember { mutableStateOf(options.showDate) }
    var showTimeOption by remember { mutableStateOf(options.showTime) }
    var showBackgroundOption by remember { mutableStateOf(options.showBackground) }
    var shadowPresetOption by remember { mutableStateOf(options.shadowPreset) }
    var shadowRadiusOption by remember { mutableFloatStateOf(options.shadowRadius) }
    var shadowDxOption by remember { mutableFloatStateOf(options.shadowDx) }
    var shadowDyOption by remember { mutableFloatStateOf(options.shadowDy) }
    var shadowAlphaOption by remember { mutableFloatStateOf(options.shadowAlpha) }
    var openAppOnClickOption by remember { mutableStateOf(options.openAppOnClick) }

    var selectedDateSize by remember { mutableFloatStateOf(options.dateTextSize) }
    var selectedTimeSize by remember { mutableFloatStateOf(options.timeTextSize) }
    var selectedTimeColor by remember { mutableStateOf(options.timeColor) }
    var selectedDateColor by remember { mutableStateOf(options.dateColor) }

    var customTimeColor by remember { mutableStateOf(options.customTimeColor) }
    var customDateColor by remember { mutableStateOf(options.customDateColor) }
    var pickingColorFor by remember { mutableStateOf<ColorTarget?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            SettingsCategory(stringResource(R.string.general))
            SwitchItem(
                title = stringResource(R.string.show_widget_background),
                isChecked = showBackgroundOption,
                icon = Icons.Rounded.Wallpaper
            ) {
                showBackgroundOption = it
            }
            TextShadowSetting(
                selectedPreset = shadowPresetOption,
                shadowDisabled = showBackgroundOption,
                onPresetChanged = { preset ->
                    shadowPresetOption = preset
                }
            )
            SwitchItem(
                title = stringResource(R.string.open_app_on_click),
                isChecked = openAppOnClickOption,
                icon = Icons.Rounded.TouchApp
            ) {
                openAppOnClickOption = it
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(stringResource(R.string.time))
            SwitchItem(
                title = stringResource(R.string.show_time),
                isChecked = showTimeOption,
                icon = Icons.Rounded.Schedule
            ) {
                showTimeOption = it
            }
            TextSizeSelectSetting(
                sizeOptions = ClockWidgetOptions.timeSizeOptions,
                title = stringResource(R.string.time_text_size),
                currentSize = selectedTimeSize
            ) {
                selectedTimeSize = it
            }
            ColorSelectSetting(
                label = stringResource(R.string.time_text_color),
                availableColors = ClockWidgetOptions.textColorOptions,
                currentColor = selectedTimeColor,
                customColorInt = customTimeColor,
                onColorSelected = { color ->
                    selectedTimeColor = color
                },
                onOpenCustomPicker = {
                    selectedTimeColor = TextColor.Custom
                    pickingColorFor = ColorTarget.TIME
                }
            )
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
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(stringResource(R.string.date))
            SwitchItem(
                title = stringResource(R.string.show_date),
                isChecked = showDateOption,
                icon = Icons.Rounded.CalendarToday
            ) {
                showDateOption = it
            }
            TextSizeSelectSetting(
                sizeOptions = ClockWidgetOptions.dateSizeOptions,
                title = stringResource(R.string.date_text_size),
                currentSize = selectedDateSize
            ) {
                selectedDateSize = it
            }
            ColorSelectSetting(
                label = stringResource(R.string.date_text_color),
                availableColors = ClockWidgetOptions.textColorOptions,
                currentColor = selectedDateColor,
                customColorInt = customDateColor,
                onColorSelected = { color ->
                    selectedDateColor = color
                },
                onOpenCustomPicker = {
                    selectedDateColor = TextColor.Custom
                    pickingColorFor = ColorTarget.DATE
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { onCancel.invoke() }) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                options.apply {
                    showDate = showDateOption
                    showTime = showTimeOption
                    dateTextSize = selectedDateSize
                    timeTextSize = selectedTimeSize
                    dateColor = selectedDateColor
                    timeColor = selectedTimeColor
                    this.customTimeColor = customTimeColor
                    this.customDateColor = customDateColor
                    timeZone = customTimeZone
                    timeZoneName = customTimeZoneName
                    showBackground = showBackgroundOption
                    shadowPreset = shadowPresetOption
                    shadowRadius = shadowRadiusOption
                    shadowDx = shadowDxOption
                    shadowDy = shadowDyOption
                    shadowAlpha = shadowAlphaOption
                    openAppOnClick = openAppOnClickOption
                }
                    onComplete.invoke(options)
                }) {
                Text(text = stringResource(R.string.save))
            }
        }
    }

    pickingColorFor?.let { target ->
        val initialColor = when (target) {
            ColorTarget.TIME -> customTimeColor ?: 0xFFFFFFFF.toInt()
            ColorTarget.DATE -> customDateColor ?: 0xFFFFFFFF.toInt()
        }

        RgbColorPickerDialog(
            initialColor = initialColor,
            onColorSelected = { selectedColorInt ->
                if (target == ColorTarget.TIME) {
                    customTimeColor = selectedColorInt
                    selectedTimeColor = TextColor.Custom
                } else {
                    customDateColor = selectedColorInt
                    selectedDateColor = TextColor.Custom
                }
                pickingColorFor = null
            },
            onDismissRequest = { pickingColorFor = null }
        )
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
    var showSizePicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.clickable(
            onClick = { showSizePicker = true }
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
                Text(
                    text = String.format("%.0f sp", currentSize),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showSizePicker) {
        var newSize = remember { currentSize }
        AlertDialog(onDismissRequest = { showSizePicker = false }, confirmButton = {
            DialogButton(label = R.string.save, style = DialogButtonStyle.PRIMARY) {
                onSizeSelected(newSize)
                showSizePicker = false
            }
        }, title = { Text(text = title) }, text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ScrollTimePicker(
                    value = sizeOptions.indexOf(currentSize).coerceAtLeast(0),
                    onValueChanged = { newSize = sizeOptions[it] },
                    maxValue = sizeOptions.size,
                    label = { sizeOptions[it].toInt().toString() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "sp",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
        })
    }
}

@Composable
fun ColorSelectSetting(
    label: String,
    availableColors: List<TextColor>,
    currentColor: TextColor,
    customColorInt: Int? = null,
    onColorSelected: (TextColor) -> Unit,
    onOpenCustomPicker: () -> Unit
) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp, 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.FormatColorText,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = label, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(availableColors) { textColor ->
                val isCustom = textColor == TextColor.Custom
                val colorValue = if (isCustom && customColorInt != null) {
                    Color(customColorInt)
                } else {
                    Color(textColor.getColorValue(context))
                }
                val isSelected = currentColor == textColor

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colorValue)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable {
                            if (isCustom) {
                                onOpenCustomPicker()
                            } else {
                                onColorSelected(textColor)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCustom && customColorInt == null && !isSelected) {
                        val iconColor = if (colorValue.luminance() > 0.5f) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Rounded.ColorLens,
                            contentDescription = stringResource(R.string.custom_color),
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (isSelected) {
                        val checkColor = if (colorValue.luminance() > 0.5f) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = checkColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onOpenCustomPicker,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ColorLens,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(6.dp))
            Text(stringResource(R.string.pick_custom_color))
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
        onCancel = {},
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
}@Composable
fun TextShadowSetting(
    selectedPreset: ShadowPreset,
    shadowDisabled: Boolean,
    onPresetChanged: (ShadowPreset) -> Unit
) {
    var showPresetDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.alpha(if (shadowDisabled) 0.38f else 1f)) {
        SwitchWithDivider(
            title = stringResource(R.string.show_text_shadow),
            description = if (shadowDisabled) {
                stringResource(R.string.shadow_unavailable_with_background)
            } else {
                stringResource(selectedPreset.label)
            },
            icon = Icons.Rounded.Layers,
            isChecked = !shadowDisabled && selectedPreset != ShadowPreset.OFF,
            onClick = {
                if (!shadowDisabled) showPresetDialog = true
            },
            onChecked = { checked ->
                if (!shadowDisabled) {
                    onPresetChanged(if (checked) ShadowPreset.SOFT else ShadowPreset.OFF)
                }
            }
        )
    }

    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            confirmButton = {
                DialogButton(label = R.string.cancel, style = DialogButtonStyle.SECONDARY) {
                    showPresetDialog = false
                }
            },
            title = { Text(text = stringResource(R.string.select_text_shadow)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ShadowPreset.entries.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPresetChanged(preset)
                                    showPresetDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preset == selectedPreset,
                                onClick = null
                            )
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = stringResource(preset.label),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(preset.description),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
