package com.bnyro.clock.domain.model

import androidx.annotation.StringRes
import com.bnyro.clock.R

enum class SortOrder(@StringRes val value: Int) {
    ALPHABETIC(R.string.alphabetic),
    OFFSET(R.string.offset)
}
