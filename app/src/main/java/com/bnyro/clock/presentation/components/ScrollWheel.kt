package com.bnyro.clock.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollWheel(
    value: Int,
    onValueChanged: (Int) -> Unit,
    maxValue: Int,
    offset: Int = 0,
    label: (Int) -> String = { String.format("%02d", it) }
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryMuted = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val hapticFeedback = LocalHapticFeedback.current
    val state = rememberPagerState(initialPage = maxValue * 100 + value - offset) {
        maxValue * 200
    }
    val currentPage = state.currentPage
    val widestLabel = remember(maxValue, offset) {
        (0 until maxValue).maxOf { label(it + offset).length }
    }
    LaunchedEffect(currentPage) {
        onValueChanged(currentPage % maxValue + offset)
        if (state.isScrollInProgress) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
    }
    VerticalPager(
        modifier = Modifier
            .height(224.dp)
            .widthIn(min = if (widestLabel >= 3) 96.dp else 0.dp),
        state = state,
        pageSpacing = 16.dp,
        pageSize = PageSize.Fixed(64.dp),
        snapPosition = SnapPosition.Center,
        flingBehavior = PagerDefaults.flingBehavior(
            state = state,
            pagerSnapDistance = PagerSnapDistance.atMost(60)
        )

    ) { index ->
        val number = index % maxValue + offset
        Text(
            text = label(number),
            style = MaterialTheme.typography.displayMedium,
            color = if (index == currentPage) primary else primaryMuted
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    ScrollWheel(value = 0, onValueChanged = {}, maxValue = 60)
}
