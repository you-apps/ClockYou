package com.bnyro.clock.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollTimePicker(
    value: Int,
    onValueChanged: (Int) -> Unit,
    maxValue: Int
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryMuted = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val state = rememberPagerState(initialPage = maxValue * 100 + value - 1)
    val currentPage = state.currentPage + 1
    val haptic = LocalHapticFeedback.current
    var firstTime by remember { mutableStateOf(true) }
    LaunchedEffect(currentPage) {
        onValueChanged(currentPage % maxValue)
        if (firstTime) {
            firstTime = false
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    VerticalPager(
        modifier = Modifier.height(224.dp),
        state = state,
        pageCount = Int.MAX_VALUE,
        pageSpacing = 16.dp,
        pageSize = PageSize.Fixed(64.dp),
        flingBehavior = PagerDefaults.flingBehavior(
            state = state,
            pagerSnapDistance = PagerSnapDistance.atMost(60)
        )

    ) { index ->
        val number = index % maxValue
        Text(
            text = String.format("%02d", number),
            style = MaterialTheme.typography.displayMedium,
            color = if (index == currentPage) primary else primaryMuted
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    ScrollTimePicker(value = 0, onValueChanged = {}, maxValue = 60)
}
