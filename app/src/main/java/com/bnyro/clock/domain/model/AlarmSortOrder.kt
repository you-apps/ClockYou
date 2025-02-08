package com.bnyro.clock.domain.model

import androidx.annotation.StringRes
import com.bnyro.clock.R

enum class AlarmSortOrder(@StringRes val value: Int) {
    HOUR_OF_DAY(R.string.hours),
    LABEL(R.string.label),
    WEEKDAY(R.string.weekdays)
}