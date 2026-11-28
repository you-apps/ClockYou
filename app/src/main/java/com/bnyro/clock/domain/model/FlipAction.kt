package com.bnyro.clock.domain.model;

import androidx.annotation.StringRes;
import com.bnyro.clock.R;

enum class FlipAction(@StringRes val resId: Int) {
    NONE(R.string.none),
    SNOOZE(R.string.snooze),
    DISMISS(R.string.dismiss)
}