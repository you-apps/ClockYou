package com.bnyro.clock

import com.bnyro.clock.data.database.AppDatabase
import com.bnyro.clock.domain.repository.AlarmRepository
import com.bnyro.clock.domain.repository.TimezoneRepository

class AppContainer(database: AppDatabase) {
    val alarmRepository: AlarmRepository by lazy {
        AlarmRepository(database.alarmsDao())
    }
    val timezoneRepository: TimezoneRepository by lazy {
        TimezoneRepository(database.timeZonesDao())
    }
}