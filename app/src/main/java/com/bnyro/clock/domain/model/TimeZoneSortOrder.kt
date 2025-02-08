package com.bnyro.clock.domain.model

import androidx.annotation.StringRes
import com.bnyro.clock.R

enum class TimeZoneSortOrder(@StringRes val value: Int) {
    ALPHABETIC(R.string.alphabetic),
    OFFSET(R.string.offset)
}
