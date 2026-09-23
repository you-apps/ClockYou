package com.bnyro.clock

import com.bnyro.clock.domain.model.TimeObject
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeObjectTest {
    @Test
    fun lapBorrowsAcrossMinuteAndHourBoundaries() {
        assertEquals(TimeObject(seconds = 59, milliseconds = 200), TimeObject(minutes = 1, milliseconds = 100) - TimeObject(milliseconds = 900))
        assertEquals(TimeObject(minutes = 59, seconds = 59, milliseconds = 200), TimeObject(hours = 1, milliseconds = 100) - TimeObject(milliseconds = 900))
        assertEquals(TimeObject(), TimeObject(hours = 1) - TimeObject(hours = 1))
    }
}
