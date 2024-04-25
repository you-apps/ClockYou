package com.bnyro.clock.presentation.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.AnalogClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AnalogClockFace
import com.bnyro.clock.domain.model.AnalogClockWidgetOptions
import com.bnyro.clock.presentation.screens.settings.model.SettingsModel
import com.bnyro.clock.ui.theme.ClockYouTheme
import com.bnyro.clock.util.ThemeUtil
import com.bnyro.clock.util.widgets.loadAnalogClockWidgetSettings
import com.bnyro.clock.util.widgets.saveAnalogClockWidgetSettings
import com.bnyro.clock.util.widgets.updateAnalogClockWidget
import android.graphics.drawable.Icon as AndroidIcon


class AnalogClockWidgetConfig : ComponentActivity() {

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

        val options = loadAnalogClockWidgetSettings(appWidgetId)
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {
                        CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.select_clock_face)) })
                    }) { pV ->
                        AnalogClockWidgetSettings(
                            modifier = Modifier.padding(pV),
                            options = options,
                            onComplete = this::complete
                        )
                    }
                }
            }
        }
    }

    private fun complete(options: AnalogClockWidgetOptions) {
        saveAnalogClockWidgetSettings(appWidgetId, options)
        updateAnalogClockWidget(appWidgetId, options)
        // return the result
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun AnalogClockWidgetSettings(
    modifier: Modifier = Modifier,
    options: AnalogClockWidgetOptions,
    onComplete: (AnalogClockWidgetOptions) -> Unit
) {

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        var clockFace by remember {
            val face = AnalogClockFace.all.let { all ->
                all.firstOrNull { it.name == options.clockFaceName } ?: all.first()
            }
            mutableStateOf(face)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LazyVerticalGrid(
                    modifier = Modifier.weight(1f),
                    columns = GridCells.Adaptive(350.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(AnalogClockFace.all) { face ->
                        AnalogClockPreview(
                            face = face,
                            selected = face == clockFace,
                            onClick = {
                                clockFace = face
                            })
                    }
                }
            }
        }
        Button(
            modifier = Modifier.padding(bottom = 16.dp),
            onClick = {
                options.apply {
                    clockFaceName = clockFace.name
                    dial = clockFace.dial
                    hourHand = clockFace.hourHand
                    minuteHand = clockFace.minuteHand
                    secondHand = clockFace.secondHand
                }
                onComplete.invoke(options)
            }) {
            Text(stringResource(R.string.save))
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AnalogClockPreview(face: AnalogClockFace, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    factory = { context ->
                        AnalogClock(context).apply {
                            if (face.dial != 0) setDial(
                                AndroidIcon.createWithResource(
                                    context,
                                    face.dial
                                )
                            )
                            if (face.hourHand != 0) setHourHand(
                                AndroidIcon.createWithResource(
                                    context,
                                    face.hourHand
                                )
                            )
                            if (face.minuteHand != 0) setMinuteHand(
                                AndroidIcon.createWithResource(
                                    context,
                                    face.minuteHand
                                )
                            )
                            if (face.secondHand != 0) setSecondHand(
                                AndroidIcon.createWithResource(
                                    context,
                                    face.secondHand
                                )
                            )
                        }
                    })
            }
            Text(face.name, style = MaterialTheme.typography.headlineSmall)
            if (face.author != null) {
                val uriHandler = LocalUriHandler.current
                Text(
                    text = stringResource(id = R.string.design_by, face.author),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable {
                        if (face.authorUrl != null) {
                            uriHandler.openUri(face.authorUrl)
                        }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
