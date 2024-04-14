package com.bnyro.clock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CountryTimezone(
    val countryName: String,
    val countryCode: String,
    val zoneId: String,
    val zoneName: String
)
