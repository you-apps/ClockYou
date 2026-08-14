package com.bnyro.clock

import android.app.Application
import android.content.Context
import android.os.Build
import com.bnyro.clock.data.database.AppDatabase
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