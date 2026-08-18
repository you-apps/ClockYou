package com.bnyro.clock.domain.model

enum class ShadowPreset(val label: String, val description: String) {
    OFF("Off", "No text shadow"),
    SUBTLE("Subtle", "Soft ambient glow"),
    SOFT("Soft", "Natural drop shadow"),
    FLOAT("Float", "Downward lighting"),
    DEEP("Deep", "High depth & blur"),
    STRONG("Strong", "High contrast")
}
