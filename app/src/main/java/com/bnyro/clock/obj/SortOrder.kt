package com.bnyro.clock.obj

import androidx.annotation.StringRes
import com.bnyro.clock.R

enum class SortOrder(@StringRes val value: Int) {
    ALPHABETIC(R.string.alphabetic),
    OFFSET(R.string.offset)
}
