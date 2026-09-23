package com.bnyro.clock

import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bnyro.clock.data.database.AppDatabase
import com.bnyro.clock.domain.model.Alarm
import com.bnyro.clock.domain.model.AlarmSortOrder
import com.bnyro.clock.domain.model.RepeatUnit
import com.bnyro.clock.presentation.screens.alarm.model.AlarmModel
import com.bnyro.clock.util.Preferences
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlarmListUpdatesTest {
    @Test
    fun upcomingUpdatesAfterTogglingEditingAndDismissingWithoutWaitingForTheClock() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<App>()
        val database = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java).build()
        val originalContainer = app.container
        app.container = AppContainer(database)
        Preferences.edit { remove(Preferences.alarmSortOrderKey) }
        val model = AlarmModel(app)
        val store = ViewModelStore()
        store.put("alarms", model)
        val subscription = launch(start = CoroutineStart.UNDISPATCHED) { model.alarms.collect {} }
        suspend fun awaitOrder(vararg ids: Long) {
            withTimeout(5000) {
                while (model.alarms.value.map { it.id } != ids.toList()) {
                    shadowOf(Looper.getMainLooper()).idle()
                    delay(10)
                }
            }
        }
        try {
            val first = Alarm(id = 1, time = 8 * 3_600_000L, enabled = true,
                startDate = LocalDate.now().plusDays(1).toEpochDay(), repeatUnit = RepeatUnit.DAY)
            database.alarmsDao().insert(first)
            database.alarmsDao().insert(first.copy(id = 2, time = 9 * 3_600_000L))
            awaitOrder(1, 2)
            assertEquals(AlarmSortOrder.UPCOMING, model.sortOrder.value)
            val displayed = model.alarms.value.first()
            model.updateAlarm(displayed.copy(enabled = false))
            awaitOrder(2, 1)
            assertTrue(displayed.enabled)
            model.updateAlarm(model.alarms.value.last().copy(enabled = true))
            awaitOrder(1, 2)
            model.updateAlarm(model.alarms.value.first().copy(time = 10 * 3_600_000L))
            awaitOrder(2, 1)
            model.dismissUpcomingAlarm(model.alarms.value.first())
            awaitOrder(1, 2)
            model.setSortOrder(AlarmSortOrder.HOUR_OF_DAY)
            awaitOrder(2, 1)
            assertEquals(AlarmSortOrder.HOUR_OF_DAY, model.sortOrder.value)
            val restoredModel = AlarmModel(app)
            store.put("restored", restoredModel)
            assertEquals(AlarmSortOrder.HOUR_OF_DAY, restoredModel.sortOrder.value)
        } finally {
            subscription.cancel()
            store.clear()
            database.close()
            app.container = originalContainer
            Preferences.edit { remove(Preferences.alarmSortOrderKey) }
        }
    }
}
