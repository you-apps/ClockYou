package com.bnyro.clock

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import com.bnyro.clock.data.database.AppDatabase
import com.bnyro.clock.presentation.widgets.AnalogClockWidget
import com.bnyro.clock.presentation.widgets.DigitalClockWidget
import com.bnyro.clock.presentation.widgets.VerticalClockWidget
import com.bnyro.clock.util.NotificationHelper
import com.bnyro.clock.util.Preferences

class App : Application() {
    lateinit var container: AppContainer

    //should work for android 6 OR all higher
    private val safeContext: Context by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createDeviceProtectedStorageContext()
        } else {
            this
        }
    }
    private val database by lazy {
        AppDatabase.getDatabase(safeContext)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            migrateToDeviceProtectedStorage()
        }

        Preferences.init(safeContext)

        NotificationHelper.createStaticNotificationChannels(this)

        container = AppContainer(database)

        updateGeneratedWidgetPreviews(this)
    }

    companion object {
        fun updateGeneratedWidgetPreviews(context: Context) {
            if (Build.VERSION.SDK_INT >= 35) {
                runCatching {
                    val appWidgetManager = AppWidgetManager.getInstance(context)

                    appWidgetManager.setWidgetPreview(
                        ComponentName(context, DigitalClockWidget::class.java),
                        AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                        RemoteViews(context.packageName, R.layout.digital_clock_preview)
                    )

                    appWidgetManager.setWidgetPreview(
                        ComponentName(context, VerticalClockWidget::class.java),
                        AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                        RemoteViews(context.packageName, R.layout.vertical_clock_preview)
                    )

                    appWidgetManager.setWidgetPreview(
                        ComponentName(context, AnalogClockWidget::class.java),
                        AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                        RemoteViews(context.packageName, R.layout.analog_clock_preview)
                    )
                }
            }
        }
    }

    private fun migrateToDeviceProtectedStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val dbName = "app_database"
            val prefName = "${packageName}_preferences"
            if (!safeContext.getDatabasePath(dbName).exists()) {
                safeContext.moveDatabaseFrom(this, dbName)
            }
            if (!safeContext.getSharedPreferences(prefName, MODE_PRIVATE).all.isNotEmpty()) {
                safeContext.moveSharedPreferencesFrom(this, prefName)
            }
        }
    }
}