package com.bnyro.clock.domain.repository

import android.content.Context
import com.bnyro.clock.data.database.dao.TimeZonesDao
import com.bnyro.clock.domain.model.CountryTimezone
import com.bnyro.clock.domain.model.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Calendar

class TimezoneRepository(private val timeZonesDao: TimeZonesDao) {
    suspend fun getTimezones(): List<TimeZone> =
        withContext(Dispatchers.IO) { timeZonesDao.getAll() }

    fun getTimezonesStream(): Flow<List<TimeZone>> = timeZonesDao.getAllStream()
    suspend fun replaceAll(vararg timeZone: TimeZone) = withContext(Dispatchers.IO) {
        timeZonesDao.clear()
        timeZonesDao.insertAll(*timeZone)
    }

    suspend fun delete(timeZone: TimeZone) =
        withContext(Dispatchers.IO) { timeZonesDao.delete(timeZone) }

    fun getTimezonesForCountries(context: Context): List<TimeZone> {
        val countryTimezones = getCountryTimezones(context)
        return getTimezonesForCountries(countryTimezones)
    }

    private fun getTimezonesForCountries(zoneIds: List<CountryTimezone>): List<TimeZone> {
        return zoneIds.map {
            val zoneKey = arrayOf(it.zoneId, it.zoneName, it.countryName).joinToString(",")
            TimeZone(zoneKey, it.zoneId, it.zoneName, it.countryName)
        }.sortedBy { it.zoneName }
    }

    private fun getCountryTimezones(context: Context): List<CountryTimezone> {
        val tzData =
            context.resources.assets.open("tz_data.json").bufferedReader()
                .use { it.readText() }

        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(tzData)
    }

}