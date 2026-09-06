package dev.openlift.app.data

import dev.openlift.app.data.local.dao.ExerciseDao
import dev.openlift.app.data.local.dao.WorkoutDao
import dev.openlift.app.data.local.entity.SetPoint
import dev.openlift.app.data.local.entity.WorkoutEntity
import dev.openlift.app.data.local.entity.WorkoutExerciseEntity
import dev.openlift.app.data.local.entity.WorkoutExerciseWithRelation
import dev.openlift.app.data.local.entity.WorkoutSetEntity
import dev.openlift.app.data.local.entity.WorkoutWithExercises
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class WorkoutRepository(
    private val dao: WorkoutDao,
    private val exerciseDao: ExerciseDao
) {

    fun observeActiveWorkout(): Flow<WorkoutEntity?> = dao.observeActiveWorkout()

    fun observeHistory(): Flow<List<WorkoutEntity>> = dao.observeHistory()

    fun observeHistorySummaries(): Flow<List<dev.openlift.app.data.local.entity.WorkoutSummaryRow>> =
        dao.observeHistorySummaries()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeWorkout(id: Long): Flow<WorkoutWithExercises?> =
        dao.observeWorkoutById(id).flatMapLatest { workout ->
            if (workout == null) {
                flowOf(null)
            } else {
                dao.observeWorkoutExercises(id).flatMapLatest { entries ->
                    if (entries.isEmpty()) {
                        flowOf(WorkoutWithExercises(workout, emptyList()))
                    } else {
                        val flows = entries.map { entry -> exerciseAggregateFlow(entry) }
                        combine(flows) { array ->
                            WorkoutWithExercises(workout, array.toList())
                        }
                    }
                }
            }
        }

    private fun exerciseAggregateFlow(entry: WorkoutExerciseEntity): Flow<WorkoutExerciseWithRelation> =
        combine(exerciseDao.observeById(entry.exerciseId), dao.observeSets(entry.id)) { exercise, sets ->
            WorkoutExerciseWithRelation(entry = entry, exercise = exercise, sets = sets)
        }

    fun observeWorkoutExercises(workoutId: Long): Flow<List<WorkoutExerciseEntity>> =
        dao.observeWorkoutExercises(workoutId)

    fun observeSets(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>> =
        dao.observeSets(workoutExerciseId)

    suspend fun createWorkout(name: String): Long =
        dao.insertWorkout(
            WorkoutEntity(name = name.ifBlank { "Workout" }, startedAt = System.currentTimeMillis())
        )

    suspend fun finishWorkout(id: Long) {
        dao.byId(id)?.let { dao.updateWorkout(it.copy(endedAt = System.currentTimeMillis())) }
    }

    suspend fun deleteWorkout(id: Long) {
        dao.byId(id)?.let { dao.deleteWorkout(it) }
    }

    /** Adds an exercise to a workout, pre-filling placeholder sets from the last session. */
    suspend fun addExercise(workoutId: Long, exerciseId: Long): Long {
        val position = (dao.maxExercisePosition(workoutId) ?: -1) + 1
        val entryId = dao.insertWorkoutExercise(
            WorkoutExerciseEntity(workoutId = workoutId, exerciseId = exerciseId, position = position)
        )
        seedPlaceholders(entryId, exerciseId)
        return entryId
    }

    /** Returns the set template (weights, reps, count) from the most recent finished session. */
    suspend fun previousSessionTemplate(exerciseId: Long): List<WorkoutSetEntity> {
        val latest = dao.latestWorkoutExercise(exerciseId) ?: return emptyList()
        return dao.setsFor(latest.id)
    }

    private suspend fun seedPlaceholders(workoutExerciseId: Long, exerciseId: Long) {
        previousSessionTemplate(exerciseId).forEachIndexed { index, previous ->
            dao.insertSet(
                WorkoutSetEntity(
                    workoutExerciseId = workoutExerciseId,
                    position = index,
                    weightKg = previous.weightKg,
                    reps = previous.reps,
                    completedAt = null,
                    isWarmup = previous.isWarmup
                )
            )
        }
    }

    suspend fun removeExercise(workoutExerciseId: Long) {
        dao.workoutExerciseById(workoutExerciseId)?.let { dao.deleteWorkoutExercise(it) }
    }

    suspend fun addSet(workoutExerciseId: Long, weightKg: Double, reps: Int, isWarmup: Boolean = false): Long {
        val position = (dao.maxSetPosition(workoutExerciseId) ?: -1) + 1
        return dao.insertSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                position = position,
                weightKg = weightKg,
                reps = reps,
                completedAt = System.currentTimeMillis(),
                isWarmup = isWarmup
            )
        )
    }

    /** Creates a placeholder (uncompleted) set, used to pre-fill from the previous session. */
    suspend fun addDraftSet(workoutExerciseId: Long, weightKg: Double, reps: Int, isWarmup: Boolean = false): Long {
        val position = (dao.maxSetPosition(workoutExerciseId) ?: -1) + 1
        return dao.insertSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                position = position,
                weightKg = weightKg,
                reps = reps,
                completedAt = null,
                isWarmup = isWarmup
            )
        )
    }

    suspend fun completeSet(setId: Long) {
        dao.completeSet(setId, System.currentTimeMillis())
    }

    suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) {
        dao.setById(setId)?.let { dao.updateSet(it.copy(weightKg = weightKg, reps = reps)) }
    }

    suspend fun toggleWarmup(setId: Long) {
        dao.setById(setId)?.let { dao.updateSet(it.copy(isWarmup = !it.isWarmup)) }
    }

    suspend fun deleteSet(setId: Long) {
        dao.setById(setId)?.let { dao.deleteSet(it) }
    }

    suspend fun setsExerciseHistory(exerciseId: Long): List<SetPoint> =
        dao.setsForExerciseHistory(exerciseId)
}
