package com.bnyro.clock

import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.data.database.AppDatabase
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmFilters
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.ui.theme.LabelColor
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlarmLabelColorFilterTest {
    @Test
    fun colorsFilterTogetherWithNamesAndRemainAvailableAcrossFilters() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<App>()
        val database = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java).build()
        val originalContainer = app.container
        app.container = AppContainer(database)
        val model = AlarmModel(app)
        val store = ViewModelStore()
        store.put("alarms", model)
        val red = LabelColor.Tomato.argb
        val blue = LabelColor.Cobalt.argb
        val custom = 0xFF123456.toInt()
        val items = listOf(
            Alarm(id = 1, time = 1000, label = "Work", labelColor = red),
            Alarm(id = 2, time = 2000, label = "Work", labelColor = blue),
            Alarm(id = 3, time = 3000, label = "Home", labelColor = red),
            Alarm(id = 4, time = 4000, label = "Custom", labelColor = custom)
        )
        val alarmsSubscription = launch(start = CoroutineStart.UNDISPATCHED) { model.alarms.collect {} }
        val colorsSubscription = launch(start = CoroutineStart.UNDISPATCHED) { model.labelColors.collect {} }
        try {
            items.forEach { database.alarmsDao().insert(it) }
            withTimeout(5000) {
                while (model.alarms.value.size != 4 || model.labelColors.value.size != 3) {
                    shadowOf(Looper.getMainLooper()).idle()
                    delay(10)
                }
            }
            model.filters.value = AlarmFilters(label = "Work", labelColors = setOf(red))
            withTimeout(5000) {
                while (model.alarms.value.map { it.id } != listOf(1L)) {
                    shadowOf(Looper.getMainLooper()).idle()
                    delay(10)
                }
            }
            assertEquals(listOf(red, blue, custom), model.labelColors.value)
            model.filters.value = AlarmFilters(labelColors = setOf(blue, custom))
            withTimeout(5000) {
                while (model.alarms.value.map { it.id } != listOf(2L, 4L)) {
                    shadowOf(Looper.getMainLooper()).idle()
                    delay(10)
                }
            }
            model.resetFilters()
            withTimeout(5000) {
                while (model.alarms.value.size != 4) {
                    shadowOf(Looper.getMainLooper()).idle()
                    delay(10)
                }
            }
            database.alarmsDao().delete(items.last())
            withTimeout(5000) {
                while (custom in model.labelColors.value) {
                    shadowOf(Looper.getMainLooper()).idle()
                    delay(10)
                }
            }
            assertEquals(listOf(red, blue), model.labelColors.value)
        } finally {
            alarmsSubscription.cancel()
            colorsSubscription.cancel()
            store.clear()
            database.close()
            app.container = originalContainer
        }
    }
}
