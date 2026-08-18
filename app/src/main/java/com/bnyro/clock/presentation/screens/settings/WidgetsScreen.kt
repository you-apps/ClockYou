package com.bnyro.clock.presentation.screens.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.AnalogClockWidgetOptions
import com.bnyro.clock.domain.model.ClockWidgetOptions
import com.bnyro.clock.presentation.components.ClickableIcon
import com.bnyro.clock.presentation.widgets.AnalogClockWidget
import com.bnyro.clock.presentation.widgets.AnalogClockWidgetConfig
import com.bnyro.clock.presentation.widgets.DigitalClockWidget
import com.bnyro.clock.presentation.widgets.DigitalClockWidgetConfig
import com.bnyro.clock.presentation.widgets.VerticalClockWidget
import com.bnyro.clock.presentation.widgets.VerticalClockWidgetConfig
import com.bnyro.clock.util.widgets.hasAnalogClockWidgetSettings
import com.bnyro.clock.util.widgets.hasClockWidgetSettings
import com.bnyro.clock.util.widgets.loadAnalogClockWidgetSettings
import com.bnyro.clock.util.widgets.loadClockWidgetSettings

sealed class PlacedWidgetInfo(
    val appWidgetId: Int,
    val titleRes: Int,
    val icon: ImageVector
) {
    data class Digital(
        val id: Int,
        val options: ClockWidgetOptions
    ) : PlacedWidgetInfo(id, R.string.digital_clock, Icons.Rounded.Schedule)

    data class Vertical(
        val id: Int,
        val options: ClockWidgetOptions
    ) : PlacedWidgetInfo(id, R.string.vertical_clock, Icons.Rounded.ViewAgenda)

    data class Analog(
        val id: Int,
        val options: AnalogClockWidgetOptions
    ) : PlacedWidgetInfo(id, R.string.analog_clock, Icons.Rounded.AccessTime)
}

private fun queryPlacedWidgets(context: Context): List<PlacedWidgetInfo> {
    val appWidgetManager = AppWidgetManager.getInstance(context) ?: return emptyList()
    val result = mutableListOf<PlacedWidgetInfo>()

    val digitalIds = appWidgetManager.getAppWidgetIds(
        ComponentName(context, DigitalClockWidget::class.java)
    )
    for (id in digitalIds) {
        if (appWidgetManager.getAppWidgetInfo(id) == null || !context.hasClockWidgetSettings(id)) {
            continue
        }
        val options = context.loadClockWidgetSettings(id, DigitalClockWidget.DefaultConfig)
        result.add(PlacedWidgetInfo.Digital(id, options))
    }

    val verticalIds = appWidgetManager.getAppWidgetIds(
        ComponentName(context, VerticalClockWidget::class.java)
    )
    for (id in verticalIds) {
        if (appWidgetManager.getAppWidgetInfo(id) == null || !context.hasClockWidgetSettings(id)) {
            continue
        }
        val options = context.loadClockWidgetSettings(id, VerticalClockWidget.DefaultConfig)
        result.add(PlacedWidgetInfo.Vertical(id, options))
    }

    val analogIds = appWidgetManager.getAppWidgetIds(
        ComponentName(context, AnalogClockWidget::class.java)
    )
    for (id in analogIds) {
        if (appWidgetManager.getAppWidgetInfo(id) == null || !context.hasAnalogClockWidgetSettings(id)) {
            continue
        }
        val options = context.loadAnalogClockWidgetSettings(id)
        result.add(PlacedWidgetInfo.Analog(id, options))
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    onClickBack: () -> Unit
) {
    val context = LocalContext.current
    var placedWidgets by remember { mutableStateOf(queryPlacedWidgets(context)) }

    val configLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            placedWidgets = queryPlacedWidgets(context)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.widgets)) },
                navigationIcon = {
                    ClickableIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack) {
                        onClickBack.invoke()
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { pv ->
        if (placedWidgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_widgets_found),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_widgets_found_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(placedWidgets, key = { it.appWidgetId }) { widget ->
                    WidgetCard(
                        widget = widget,
                        onEdit = {
                            val intent = when (widget) {
                                is PlacedWidgetInfo.Digital -> Intent(
                                    context,
                                    DigitalClockWidgetConfig::class.java
                                )
                                is PlacedWidgetInfo.Vertical -> Intent(
                                    context,
                                    VerticalClockWidgetConfig::class.java
                                )
                                is PlacedWidgetInfo.Analog -> Intent(
                                    context,
                                    AnalogClockWidgetConfig::class.java
                                )
                            }.apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.appWidgetId)
                            }
                            configLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetCard(
    widget: PlacedWidgetInfo,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = widget.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = stringResource(widget.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    val summary = when (widget) {
                        is PlacedWidgetInfo.Digital -> {
                            val tz = if (widget.options.timeZone != null) widget.options.timeZoneName else null
                            if (tz != null) "Timezone: $tz" else "System Timezone"
                        }
                        is PlacedWidgetInfo.Vertical -> {
                            val tz = if (widget.options.timeZone != null) widget.options.timeZoneName else null
                            if (tz != null) "Timezone: $tz" else "System Timezone"
                        }
                        is PlacedWidgetInfo.Analog -> {
                            "Clock Face: ${widget.options.clockFaceName}"
                        }
                    }
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FilledTonalButton(
                onClick = onEdit,
                shape = ButtonDefaults.filledTonalShape,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(R.string.edit_widget),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
