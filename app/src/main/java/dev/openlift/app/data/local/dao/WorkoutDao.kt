package dev.openlift.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.openlift.app.data.local.entity.SetPoint
import dev.openlift.app.data.local.entity.WorkoutEntity
import dev.openlift.app.data.local.entity.WorkoutExerciseEntity
import dev.openlift.app.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE ended_at IS NULL ORDER BY started_at DESC LIMIT 1")
    fun observeActiveWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE ended_at IS NULL ORDER BY started_at DESC LIMIT 1")
    suspend fun activeWorkout(): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE ended_at IS NOT NULL ORDER BY started_at DESC")
    fun observeHistory(): Flow<List<WorkoutEntity>>    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun byId(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeWorkoutById(id: Long): Flow<WorkoutEntity?>

    @Insert
    suspend fun insertWorkoutExercise(entry: WorkoutExerciseEntity): Long

    @Update
    suspend fun updateWorkoutExercise(entry: WorkoutExerciseEntity)

    @Delete
    suspend fun deleteWorkoutExercise(entry: WorkoutExerciseEntity)

    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId ORDER BY position")
    suspend fun workoutExercises(workoutId: Long): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId ORDER BY position")
    fun observeWorkoutExercises(workoutId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun workoutExerciseById(id: Long): WorkoutExerciseEntity?

    @Query("SELECT MAX(position) FROM workout_exercises WHERE workout_id = :workoutId")
    suspend fun maxExercisePosition(workoutId: Long): Int?

    @Query("SELECT we.* FROM workout_exercises we JOIN workouts w ON we.workout_id = w.id WHERE we.exercise_id = :exerciseId AND w.ended_at IS NOT NULL ORDER BY w.started_at DESC LIMIT 1")
    suspend fun latestWorkoutExercise(exerciseId: Long): WorkoutExerciseEntity?

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteSets(sets: List<WorkoutSetEntity>)

    @Query("SELECT * FROM workout_sets WHERE workout_exercise_id = :id ORDER BY position")
    fun observeSets(id: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE workout_exercise_id = :id ORDER BY position")
    suspend fun setsFor(id: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE id = :id")
    suspend fun setById(id: Long): WorkoutSetEntity?

    @Query("UPDATE workout_sets SET completed_at = :time WHERE id = :id")
    suspend fun completeSet(id: Long, time: Long)

    @Query("SELECT MAX(position) FROM workout_sets WHERE workout_exercise_id = :id")
    suspend fun maxSetPosition(id: Long): Int?

    @Query(
        """
        SELECT w.started_at AS date, w.id AS workoutId, s.weight_kg AS weightKg,
               s.reps AS reps, s.is_warmup AS isWarmup
        FROM workout_sets s
        JOIN workout_exercises we ON s.workout_exercise_id = we.id
        JOIN workouts w ON we.workout_id = w.id
        WHERE we.exercise_id = :exerciseId AND w.ended_at IS NOT NULL AND s.completed_at IS NOT NULL
        ORDER BY w.started_at ASC
        """
    )
    suspend fun setsForExerciseHistory(exerciseId: Long): List<SetPoint>

    @Query("SELECT * FROM workouts ORDER BY started_at")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Query("SELECT * FROM workout_exercises")
    suspend fun getAllWorkoutExercises(): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM workout_sets")
    suspend fun getAllSets(): List<WorkoutSetEntity>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBackupWorkouts(entities: List<WorkoutEntity>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBackupExercises(entities: List<WorkoutExerciseEntity>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBackupSets(entities: List<WorkoutSetEntity>)

    @Query("DELETE FROM workout_sets")
    suspend fun deleteAllSets()

    @Query("DELETE FROM workout_exercises")
    suspend fun deleteAllWorkoutExercises()

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    @Query(
        """
        SELECT w.id AS workoutId, w.name, w.started_at AS startedAt, w.ended_at AS endedAt,
          (SELECT COUNT(DISTINCT we.id) FROM workout_exercises we WHERE we.workout_id = w.id) AS exerciseCount,
          (SELECT COALESCE(SUM(s.weight_kg * s.reps), 0) FROM workout_sets s
             JOIN workout_exercises we2 ON s.workout_exercise_id = we2.id
             WHERE we2.workout_id = w.id AND s.is_warmup = 0) AS totalKg,
          (SELECT COUNT(*) FROM workout_sets s2
             JOIN workout_exercises we3 ON s2.workout_exercise_id = we3.id
             WHERE we3.workout_id = w.id AND s2.is_warmup = 0) AS setCount
        FROM workouts w
        WHERE w.ended_at IS NOT NULL
        ORDER BY w.started_at DESC
        """
    )
    fun observeHistorySummaries(): Flow<List<dev.openlift.app.data.local.entity.WorkoutSummaryRow>>
}
