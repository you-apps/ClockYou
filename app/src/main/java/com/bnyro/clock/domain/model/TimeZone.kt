package com.bnyro.clock.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeZones")
data class TimeZone(
    @PrimaryKey
    val key: String,
    val zoneId: String,
    val zoneName: String,
    val countryName: String = "",
) {
    override fun equals(other: Any?): Boolean {
        if (other is TimeZone) {
            return this.key == other.key
        }

        return super.equals(other)
    }
}
