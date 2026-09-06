package dev.openlift.app.domain

import dev.openlift.app.data.local.entity.ExerciseEntity
import dev.openlift.app.data.local.entity.SetPoint

data class ExerciseGroup(
    val muscleGroup: String,
    val exercises: List<ExerciseEntity>
)

object PickerOrder {

    val MUSCLE_ORDER = listOf(
        "Chest", "Back", "Shoulders", "Quads", "Hamstrings", "Glutes",
        "Calves", "Biceps", "Triceps", "Forearms", "Core", "Full Body"
    )

    fun filterLongestFirst(query: String, exercises: List<ExerciseEntity>): List<ExerciseEntity> {
        val q = query.trim()
        if (q.isEmpty()) return exercises
        return exercises.filter {
            it.name.contains(q, ignoreCase = true) || it.muscleGroup.contains(q, ignoreCase = true)
        }
    }

    fun group(exercises: List<ExerciseEntity>): List<ExerciseGroup> {
        val grouped = exercises.groupBy { it.muscleGroup.ifBlank { "Other" } }
        return grouped.map { (group, list) ->
            ExerciseGroup(group, list.sortedBy { it.name.lowercase() })
        }.sortedBy { MUSCLE_ORDER.indexOf(it.muscleGroup).let { i -> if (i < 0) Int.MAX_VALUE else i } }
    }
}

data class E1RmPoint(
    val workoutId: Long,
    val date: Long,
    val valueKg: Double
)

object ProgressCalculator {

    fun sessionPoints(sets: List<SetPoint>): List<E1RmPoint> {
        return sets.filter { !it.isWarmup }
            .groupBy { it.workoutId }
            .map { (workoutId, list) ->
                val date = list.minOf { it.date }
                val value = OneRepMax.formatE1rm(
                    OneRepMax.maxOf(list.map { p -> p.weightKg to p.reps })
                )
                E1RmPoint(workoutId, date, value)
            }
            .sortedBy { it.date }
    }

    fun best(points: List<E1RmPoint>): E1RmPoint? = points.maxByOrNull { it.valueKg }

    fun latest(points: List<E1RmPoint>): E1RmPoint? = points.lastOrNull()

    fun deltaKg(points: List<E1RmPoint>): Double {
        val latest = points.lastOrNull()?.valueKg ?: return 0.0
        val first = points.firstOrNull()?.valueKg ?: return 0.0
        return latest - first
    }
}
