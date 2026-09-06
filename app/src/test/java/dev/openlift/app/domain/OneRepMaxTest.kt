package dev.openlift.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OneRepMaxTest {

    @Test
    fun `epley at one rep equals weight`() {
        assertEquals(100.0, OneRepMax.epley(100.0, 1), 0.001)
    }

    @Test
    fun `epley formula correctness`() {
        // weight * (1 + reps/30)
        assertEquals(133.333, OneRepMax.epley(100.0, 10), 0.01)
        assertEquals(150.0, OneRepMax.epley(100.0, 15), 0.01)
    }

    @Test
    fun `brzycki formula correctness`() {
        // weight * 36 / (37 - reps)
        assertEquals(133.333, OneRepMax.brzycki(100.0, 10), 0.01)
        assertEquals(100.0, OneRepMax.brzycki(100.0, 1), 0.001)
    }

    @Test
    fun `maxOf ignores empty`() {
        assertEquals(0.0, OneRepMax.maxOf(emptyList()), 0.001)
    }

    @Test
    fun `maxOf finds the highest estimate`() {
        val entries = listOf(80.0 to 10, 120.0 to 3, 100.0 to 5)
        assertEquals(120.0 * (1 + 3.0 / 30.0), OneRepMax.maxOf(entries), 0.01)
    }

    @Test
    fun `formatE1rm rounds to half kg`() {
        assertEquals(133.5, OneRepMax.formatE1rm(133.333), 0.001)
        assertEquals(100.0, OneRepMax.formatE1rm(100.0), 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `epley throws on zero reps`() {
        OneRepMax.epley(100.0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `epley throws on negative weight`() {
        OneRepMax.epley(-10.0, 5)
    }

    @Test
    fun `sessionE1rm returns formatted`() {
        assertEquals(133.5, OneRepMax.sessionE1rm(listOf(100.0 to 10)), 0.001)
    }

    @Test
    fun `sessionE1rm returns zero when no entries`() {
        assertEquals(0.0, OneRepMax.sessionE1rm(emptyList()), 0.001)
        assertTrue(OneRepMax.maxOf(emptyList()) == 0.0)
    }
}
