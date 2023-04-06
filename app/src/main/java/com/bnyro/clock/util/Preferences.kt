package com.bnyro.clock.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.bnyro.clock.ext.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

object Preferences {
    val TIME_ZONES = stringSetPreferencesKey("time_zones")

    lateinit var instance: DataStore<androidx.datastore.preferences.core.Preferences>
    lateinit var preferences: Flow<androidx.datastore.preferences.core.Preferences>

    fun init(context: Context) {
        instance = context.dataStore
        preferences = instance.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
    }
}
