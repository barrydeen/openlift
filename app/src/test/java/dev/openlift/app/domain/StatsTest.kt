package dev.openlift.app.domain

import dev.openlift.app.data.local.entity.WorkoutExerciseEntity
import dev.openlift.app.data.local.entity.WorkoutSetEntity
import dev.openlift.app.data.local.entity.WorkoutWithExercises
import dev.openlift.app.data.local.entity.WorkoutExerciseWithRelation
import dev.openlift.app.data.local.entity.WorkoutEntity
import dev.openlift.app.data.local.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsTest {

    private fun workout(vararg exerciseSets: List<WorkoutSetEntity>): WorkoutWithExercises {
        val weList = exerciseSets.map { sets ->
            WorkoutExerciseWithRelation(
                entry = WorkoutExerciseEntity(id = sets.firstOrNull()?.let { 1L } ?: 1L, workoutId = 1L, exerciseId = 1L, position = 0),
                exercise = ExerciseEntity(id = 1L, name = "Ex", muscleGroup = "Chest", equipment = "BARBELL"),
                sets = sets
            )
        }
        return WorkoutWithExercises(
            workout = WorkoutEntity(id = 1L, name = "Test", startedAt = 0L, endedAt = 1000L),
            exercises = weList
        )
    }

    private fun set(weight: Double, reps: Int, warmup: Boolean = false) =
        WorkoutSetEntity(id = 1L, workoutExerciseId = 1L, position = 1, weightKg = weight, reps = reps, completedAt = 100L, isWarmup = warmup)

    @Test
    fun `volume excludes warmups`() {
        val w = workout(
            listOf(set(100.0, 5), set(40.0, 10, warmup = true)),
            listOf(set(50.0, 8))
        )
        val volume = Stats.volumeOf(w)
        assertEquals(100.0 * 5 + 50.0 * 8, volume.totalKg, 0.001)
        assertEquals(2, volume.sets)
        assertEquals(13, volume.reps)
    }

    @Test
    fun `e1rm of workout uses best working set`() {
        val w = workout(
            listOf(set(100.0, 10), set(180.0, 1)),
            listOf(set(90.0, 5))
        )
        val e1rm = Stats.e1rmOf(w)
        assertEquals(OneRepMax.formatE1rm(180.0), e1rm, 0.001)
    }

    @Test
    fun `exercise count`() {
        val w = workout(listOf(set(10.0, 5)), listOf(set(20.0, 5)))
        assertEquals(2, Stats.exerciseCount(w))
    }
}
