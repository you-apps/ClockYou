package com.bnyro.clock.util

import android.content.Context
import com.bnyro.clock.domain.model.VibrationPattern
import kotlinx.serialization.json.Json

class VibrationPatternHelper {

    fun getVibrationPatterns(context: Context): List<VibrationPattern> {
        return loadVibrationPatterns(context).entries.map { (name, pattern) ->
            VibrationPattern(name, pattern)
        }
    }

    private fun loadVibrationPatterns(context: Context): Map<String, List<Int>> {
        val vibrationPatterns =
            context.resources.assets.open("vibration_patterns.json").bufferedReader()
                .use { it.readText() }

        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(vibrationPatterns)
    }
}