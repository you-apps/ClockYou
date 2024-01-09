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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollTimePicker(
   initialPage: Int,
   onValueChanged: (Int) -> Unit,
   maxValue: Int,
   offset: Int = 0
) {
   val primary = MaterialTheme.colorScheme.primary
   val primaryMuted = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
   val state = rememberPagerState(initialPage = initialPage)
   val currentPage = state.currentPage + 1
   LaunchedEffect(currentPage) {
       val value = (currentPage + offset) % maxValue
       onValueChanged(value)
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
       val number = index % maxValue + offset
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
   val initialPage = 60 * 100 + 0 - 1 - 0
   ScrollTimePicker(initialPage = initialPage, onValueChanged = {}, maxValue = 60)
}
