package com.bnyro.clock.domain.model

import androidx.annotation.PluralsRes
import com.bnyro.clock.R

enum class RepeatUnit(@PluralsRes val value: Int, @PluralsRes val summary: Int) {
    DAY(R.plurals.repeat_unit_days, R.plurals.every_days),
    WEEK(R.plurals.repeat_unit_weeks, R.plurals.every_weeks),
    MONTH(R.plurals.repeat_unit_months, R.plurals.every_months),
    YEAR(R.plurals.repeat_unit_years, R.plurals.every_years)
}
