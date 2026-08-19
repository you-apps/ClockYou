package com.bnyro.clock.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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
    // a fling may coast as far as it likes, but it may never settle back behind the
    // value that was highlighted when the finger left the wheel
    val snapDistance = remember(state) {
        object : PagerSnapDistance {
            override fun calculateTargetPage(
                startPage: Int,
                suggestedTargetPage: Int,
                velocity: Float,
                pageSize: Int,
                pageSpacing: Int
            ): Int {
                val coasted = PagerSnapDistance.atMost(60).calculateTargetPage(
                    startPage, suggestedTargetPage, velocity, pageSize, pageSpacing
                )
                val highlighted = state.currentPage
                return when {
                    highlighted > startPage -> maxOf(coasted, highlighted)
                    highlighted < startPage -> minOf(coasted, highlighted)
                    else -> coasted
                }
            }
        }
    }

    var pageAtGestureStart by remember { mutableIntStateOf(state.currentPage) }
    LaunchedEffect(state.isScrollInProgress) {
        if (state.isScrollInProgress) pageAtGestureStart = state.currentPage
    }

    val coastingFling = PagerDefaults.flingBehavior(state = state, pagerSnapDistance = snapDistance)
    // a finger that slows to a stop can leave with a little speed pointing back the way it
    // came, which would coast a row backwards before the floor above pulled it forward again
    val fling = remember(coastingFling) {
        object : TargetedFlingBehavior {
            private fun releasedWith(initialVelocity: Float): Float {
                val advanced = state.currentPage > pageAtGestureStart
                val retreated = state.currentPage < pageAtGestureStart
                val turnsBack =
                    (advanced && initialVelocity > 0f) || (retreated && initialVelocity < 0f)
                return if (turnsBack) 0f else initialVelocity
            }

            override suspend fun ScrollScope.performFling(
                initialVelocity: Float,
                onRemainingDistanceUpdated: (Float) -> Unit
            ): Float = with(coastingFling) {
                performFling(releasedWith(initialVelocity), onRemainingDistanceUpdated)
            }
        }
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
        flingBehavior = fling

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
