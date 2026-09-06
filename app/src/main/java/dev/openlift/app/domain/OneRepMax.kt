package dev.openlift.app.domain

import kotlin.math.max

/**
 * Estimates one-repetition maximum from a sub-maximal set using the Epley formula.
 */
object OneRepMax {

    /**
     * Epley formula: 1RM = weight * (1 + reps / 30)
     * Equals [weightKg] when [reps] is 1.
     */
    fun epley(weightKg: Double, reps: Int): Double {
        require(weightKg >= 0.0) { "weight must be non-negative" }
        require(reps >= 1) { "reps must be at least 1" }
        if (reps == 1) return weightKg
        return weightKg * (1.0 + reps / 30.0)
    }

    /**
     * Brzycki formula: 1RM = weight * 36 / (37 - reps)
     */
    fun brzycki(weightKg: Double, reps: Int): Double {
        require(weightKg >= 0.0) { "weight must be non-negative" }
        require(reps in 1..36) { "reps must be between 1 and 36" }
        if (reps == 1) return weightKg
        return weightKg * 36.0 / (37.0 - reps)
    }

    /**
     * Returns the highest estimated 1RM across a collection of weight/reps points.
     * Warm-up sets are ignored by passing only working sets.
     */
    fun maxOf(entries: List<Pair<Double, Int>>): Double =
        entries.mapNotNull { (w, r) -> epley(w, r) }.maxOrNull() ?: 0.0

    /**
     * Returns the projected 1RM rounded to the nearest 0.5 kg for display.
     */
    fun formatE1rm(weightKg: Double): Double = (weightKg * 2).let { Math.round(it) / 2.0 }

    fun sessionE1rm(entries: List<Pair<Double, Int>>): Double = formatE1rm(maxOf(entries))

    private fun epleyOrNull(weightKg: Double, reps: Int): Double? {
        if (weightKg <= 0.0 || reps <= 0) return null
        return epley(weightKg, reps)
    }

    private fun normalize(value: Double): Double =
        if (value > 0.0) max(value, 0.0) else 0.0
}
