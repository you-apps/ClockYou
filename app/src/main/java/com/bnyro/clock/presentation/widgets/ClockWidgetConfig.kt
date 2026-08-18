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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.domain.model.ShadowPreset
import com.bnyro.clock.presentation.components.ModernStepSlider
import com.bnyro.clock.presentation.components.RgbColorPickerDialog
import com.bnyro.clock.presentation.components.SwitchItem
import com.bnyro.clock.presentation.components.SwitchWithDivider
import com.bnyro.clock.presentation.screens.clock.components.TimeZonePickerDialog
import com.bnyro.clock.presentation.screens.clock.model.ClockModel
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
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
        if (!hasClockWidgetSettings(appWidgetId)) {
            applyToWidget(this, options)
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
                        CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.digital_clock_widget)) })
                    }) { pV ->
                        DigitalClockWidgetSettings(
                            modifier = Modifier.padding(pV),
                            options = options
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
                icon = Icons.Rounded.Schedule
            ) {
                showTimeOption = it
            }
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
        }
        Button(
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            onClick = {
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
            Text(stringResource(R.string.save))
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
    customColorInt: Int? = null,
    onColorSelected: (TextColor) -> Unit,
    onOpenCustomPicker: () -> Unit
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
/**
 * Modern Material 3 Expressive Text Shadow setting:
 * - Master switch item with Layers icon and background-disabled warning
 * - M3 Expressive Segmented/Connected Group Card pattern:
 *   - Outer radius: 28dp (extra-large), Inner adjacent radius: 4dp (concentric)
 *   - Separated trigger card and content card with 4dp gap
 *   - Smooth corner-radius morphing (28dp -> 4dp) on expand/collapse
 *   - Circular tonal avatar icon badge
 *   - True working step slider for the 5 shadow intensity presets:
 *     1. Subtle (Soft ambient glow)
 *     2. Soft (Natural drop shadow)
 *     3. Float (Downward lighting)
 *     4. Deep (High depth & blur)
 *     5. Strong (High contrast outline)
 */
@Composable
fun TextShadowSetting(
    selectedPreset: ShadowPreset,
    shadowDisabled: Boolean,
    onPresetChanged: (ShadowPreset) -> Unit
) {
    val isEnabled = selectedPreset != ShadowPreset.OFF && !shadowDisabled
    var showAdvanced by remember { mutableStateOf(false) }
    val alpha = if (shadowDisabled) 0.38f else 1f

    val chevronRotation by animateFloatAsState(
        targetValue = if (showAdvanced) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "chevronRotation"
    )
    // Morph bottom corners of trigger from 28.dp to 4.dp when expanded
    val triggerBottomRadius by animateDpAsState(
        targetValue = if (showAdvanced) 4.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "triggerBottomRadius"
    )

    val activePresetIndex = when (selectedPreset) {
        ShadowPreset.SUBTLE -> 1f
        ShadowPreset.SOFT -> 2f
        ShadowPreset.FLOAT -> 3f
        ShadowPreset.DEEP -> 4f
        ShadowPreset.STRONG -> 5f
        ShadowPreset.OFF -> 2f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
    ) {
        SwitchItem(
            title = stringResource(R.string.show_text_shadow),
            description = if (shadowDisabled) stringResource(R.string.shadow_unavailable_with_background) else null,
            isChecked = isEnabled,
            icon = Icons.Rounded.Layers
        ) { checked ->
            if (!shadowDisabled) {
                if (checked) {
                    onPresetChanged(ShadowPreset.SOFT)
                } else {
                    onPresetChanged(ShadowPreset.OFF)
                }
            }
        }

        if (isEnabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // 1. Trigger Card (Top)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp,
                        bottomStart = triggerBottomRadius,
                        bottomEnd = triggerBottomRadius
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvanced = !showAdvanced }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.size(14.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.advanced_shadow_settings),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Style: ${selectedPreset.label} — ${selectedPreset.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .rotate(chevronRotation),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // 2. Content Card (Separated with concentric gap and radii)
                AnimatedVisibility(
                    visible = showAdvanced,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 4.dp,
                                bottomStart = 28.dp,
                                bottomEnd = 28.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                ModernStepSlider(
                                    title = "Shadow Intensity & Style",
                                    value = activePresetIndex,
                                    onValueChange = { index ->
                                        val rounded = index.toInt().coerceIn(1, 5)
                                        val newPreset = when (rounded) {
                                            1 -> ShadowPreset.SUBTLE
                                            2 -> ShadowPreset.SOFT
                                            3 -> ShadowPreset.FLOAT
                                            4 -> ShadowPreset.DEEP
                                            5 -> ShadowPreset.STRONG
                                            else -> ShadowPreset.SOFT
                                        }
                                        onPresetChanged(newPreset)
                                    },
                                    valueRange = 1f..5f,
                                    steps = 3,
                                    valueLabel = selectedPreset.label,
                                    startLabel = "Subtle",
                                    centerLabel = "Float",
                                    endLabel = "Strong"
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = selectedPreset.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}