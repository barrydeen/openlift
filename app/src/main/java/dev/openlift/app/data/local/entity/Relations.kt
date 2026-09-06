package dev.openlift.app.data.local.entity

data class WorkoutExerciseWithRelation(
    val entry: WorkoutExerciseEntity,
    val exercise: ExerciseEntity?,
    val sets: List<WorkoutSetEntity>
)

data class WorkoutWithExercises(
    val workout: WorkoutEntity,
    val exercises: List<WorkoutExerciseWithRelation>
)

data class SetPoint(
    val date: Long,
    val workoutId: Long,
    val weightKg: Double,
    val reps: Int,
    val isWarmup: Boolean
)

data class ExerciseUsageCount(
    val exerciseId: Long,
    val useCount: Int
)

data class WorkoutSummaryRow(
    val workoutId: Long,
    val name: String,
    val startedAt: Long,
    val endedAt: Long?,
    val totalKg: Double,
    val setCount: Int,
    val exerciseCount: Int
)
