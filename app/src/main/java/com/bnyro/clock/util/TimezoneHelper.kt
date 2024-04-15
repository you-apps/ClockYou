package com.bnyro.clock.util

import android.content.Context
import com.bnyro.clock.domain.model.CountryTimezone
import kotlinx.serialization.json.Json

fun getCountryTimezones(context: Context): List<CountryTimezone> {
    val tzData =
        context.resources.assets.open("tz_data.json").bufferedReader()
            .use { it.readText() }

    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(tzData)
}
