package dev.openlift.app.domain

import dev.openlift.app.data.local.entity.SetPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    private fun point(workoutId: Long, date: Long, w: Double, reps: Int, warmup: Boolean = false) =
        SetPoint(date = date, workoutId = workoutId, weightKg = w, reps = reps, isWarmup = warmup)

    @Test
    fun `sessionPoints groups by workout and takes max e1rm`() {
        val points = listOf(
            point(1, 1000, 100.0, 10),
            point(1, 1000, 120.0, 3),
            point(2, 2000, 110.0, 10)
        )
        val sessions = ProgressCalculator.sessionPoints(points)
        assertEquals(2, sessions.size)
        assertEquals(1000L, sessions[0].date)
        assertEquals(2000L, sessions[1].date)
    }

    @Test
    fun `warmup sets are excluded`() {
        val points = listOf(
            point(1, 1000, 40.0, 10, warmup = true),
            point(1, 1000, 100.0, 10)
        )
        val sessions = ProgressCalculator.sessionPoints(points)
        assertEquals(1, sessions.size)
        // Should reflect the working set only, not the warm-up.
        assertEquals(OneRepMax.formatE1rm(OneRepMax.epley(100.0, 10)), sessions[0].valueKg, 0.001)
    }

    @Test
    fun `sessionPoints sorted by date`() {
        val points = listOf(point(2, 2000, 100.0, 5), point(1, 1000, 100.0, 5))
        val sessions = ProgressCalculator.sessionPoints(points)
        assertEquals(listOf(1000L, 2000L), sessions.map { it.date })
    }

    @Test
    fun `best latest delta`() {
        val points = listOf(
            E1RmPoint(1, 1000, 100.0),
            E1RmPoint(2, 2000, 120.0)
        )
        assertEquals(120.0, ProgressCalculator.best(points)!!.valueKg, 0.001)
        assertEquals(2000L, ProgressCalculator.latest(points)!!.date)
        assertEquals(20.0, ProgressCalculator.deltaKg(points), 0.001)
    }

    @Test
    fun `delta is zero for empty or single`() {
        assertEquals(0.0, ProgressCalculator.deltaKg(emptyList()), 0.001)
        assertEquals(0.0, ProgressCalculator.deltaKg(listOf(E1RmPoint(1, 1000, 100.0))), 0.001)
    }
}
