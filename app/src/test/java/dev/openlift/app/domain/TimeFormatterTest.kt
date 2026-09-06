package dev.openlift.app.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class TimeFormatterTest {

    @Test
    fun `duration under a minute`() {
        val start = 0L
        val end = 45_000L
        assertTrue(TimeFormatter.duration(start, end) == "0:45")
    }

    @Test
    fun `duration with minutes and seconds`() {
        val end = 3 * 60_000L + 20_000L
        assertTrue(TimeFormatter.duration(0L, end) == "3:20")
    }

    @Test
    fun `duration in hours`() {
        val end = 2L * 3_600_000L + 15 * 60_000L
        assertTrue(TimeFormatter.duration(0L, end) == "2h 15m")
    }

    @Test
    fun `countdown format`() {
        assertTrue(TimeFormatter.countdown(90) == "1:30")
        assertTrue(TimeFormatter.countdown(5) == "0:05")
        assertTrue(TimeFormatter.countdown(65) == "1:05")
    }
}
