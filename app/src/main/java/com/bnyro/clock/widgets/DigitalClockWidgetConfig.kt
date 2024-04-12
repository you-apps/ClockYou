package com.bnyro.clock.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.ui.common.SwitchItem
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.ThemeUtil
import com.bnyro.clock.widgets.DigitalClockWidgetConfig.Companion.InitialTextSize

class DigitalClockWidgetConfig : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )?.let {
            appWidgetId = it
        }
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        // get settings
        val showDate = sharedPreferences.getBoolean(PREF_SHOW_DATE + appWidgetId, true)
        val textSize =
            sharedPreferences.getFloat(PREF_DATE_TEXT_SIZE + appWidgetId, InitialTextSize)

        setContent {
            DigitalClockWidgetSettings(
                showDateSetting = showDate, dateTextSize = textSize, onComplete = this::complete
            )
        }


    }

    private fun complete(showDate: Boolean, textSize: Float) {
        // Save the settings
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_SHOW_DATE + appWidgetId, showDate)
        editor.putFloat(PREF_DATE_TEXT_SIZE + appWidgetId, textSize)
        editor.apply()

        // update the widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val views = RemoteViews(packageName, R.layout.digital_clock)

        val visibility = if (showDate) View.VISIBLE else View.GONE
        views.setViewVisibility(R.id.textClock, visibility)

        views.setTextViewTextSize(R.id.textClock, TypedValue.COMPLEX_UNIT_SP, textSize)

        appWidgetManager.updateAppWidget(appWidgetId, views)

        // return the result
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val PREF_FILE = "WidgetConfig"
        const val PREF_SHOW_DATE = "showDate:"
        const val PREF_DATE_TEXT_SIZE = "dateTextSize:"

        const val InitialTextSize = 16f
    }
}


@Composable
fun DigitalClockWidgetSettings(
    showDateSetting: Boolean,
    dateTextSize: Float,
    onComplete: (showDate: Boolean, textSize: Float) -> Unit
) {
    val settingsModel: SettingsModel = viewModel()
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
            var showDate by remember { mutableStateOf(showDateSetting) }
            var selectedSize by remember { mutableStateOf(dateTextSize) }
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    SwitchItem(
                        title = stringResource(R.string.show_date),
                        isChecked = showDate,
                        icon = Icons.Rounded.CalendarToday
                    ) {
                        showDate = it
                    }
                    TextSizeSelectSetting(currentSize = selectedSize) {
                        selectedSize = it
                    }
                }
                Button(
                    modifier = Modifier.padding(bottom = 16.dp),
                    onClick = {
                        onComplete.invoke(
                            showDate,
                            selectedSize
                        )
                    }) {
                    Text(stringResource(R.string.save))
                }
            }

        }
    }
}

@Composable
fun TextSizeSelectSetting(
    currentSize: Float,
    onSizeSelected: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val sizeOptions = listOf(
        12f,
        14f,
        16f,
        18f,
        20f,
        22f,
        24f
    )
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
                    text = stringResource(R.string.date_text_size),
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
                    fontSize = currentSize.sp
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
                                    fontSize = size.sp
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
        showDateSetting = true,
        dateTextSize = InitialTextSize,
        onComplete = { _, _ ->
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TextSizeSelectSettingPreview() {
    TextSizeSelectSetting(
        currentSize = InitialTextSize,
        onSizeSelected = {}
    )
}