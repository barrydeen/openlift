package dev.openlift.app.domain

import dev.openlift.app.data.local.entity.WorkoutSetEntity
import dev.openlift.app.data.local.entity.WorkoutWithExercises

object Stats {

    data class Volume(val totalKg: Double, val sets: Int, val reps: Int) {
        val totalLb: Double get() = UnitConverter.fromKg(totalKg, WeightUnit.LB)
    }

    fun volumeOf(workout: WorkoutWithExercises): Volume {
        var total = 0.0
        var sets = 0
        var reps = 0
        for (ex in workout.exercises) {
            for (s in ex.sets) {
                if (s.isWarmup) continue
                total += s.weightKg * s.reps
                sets++
                reps += s.reps
            }
        }
        return Volume(total, sets, reps)
    }

    fun volumeOf(exercises: List<WorkoutSetEntity>): Volume {
        var total = 0.0
        var sets = 0
        var reps = 0
        for (s in exercises) {
            if (s.isWarmup) continue
            total += s.weightKg * s.reps
            sets++
            reps += s.reps
        }
        return Volume(total, sets, reps)
    }

    /** Highest estimated 1RM across a workout's working sets. */
    fun e1rmOf(workout: WorkoutWithExercises): Double {
        val points = mutableListOf<Pair<Double, Int>>()
        for (ex in workout.exercises) {
            for (s in ex.sets) {
                if (s.isWarmup) continue
                points.add(s.weightKg to s.reps)
            }
        }
        return OneRepMax.formatE1rm(OneRepMax.maxOf(points))
    }

    fun exerciseCount(workout: WorkoutWithExercises): Int = workout.exercises.size

    fun perExerciseVolume(exerciseSets: List<WorkoutSetEntity>): Volume = volumeOf(exerciseSets)
}
