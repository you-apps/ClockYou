package com.bnyro.clock.domain.model

import androidx.annotation.StringRes
import com.bnyro.clock.R

enum class ShadowPreset(@StringRes val label: Int, @StringRes val description: Int) {
    OFF(R.string.shadow_off, R.string.shadow_off_description),
    SUBTLE(R.string.shadow_subtle, R.string.shadow_subtle_description),
    SOFT(R.string.shadow_soft, R.string.shadow_soft_description),
    FLOAT(R.string.shadow_float, R.string.shadow_float_description),
    DEEP(R.string.shadow_deep, R.string.shadow_deep_description),
    STRONG(R.string.shadow_strong, R.string.shadow_strong_description)
}
