package dev.openlift.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.openlift.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<ExerciseEntity>): List<Long>

    @Insert
    suspend fun insert(entity: ExerciseEntity): Long

    @Query("SELECT * FROM exercises ORDER BY muscle_group, name")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observeById(id: Long): Flow<ExerciseEntity?>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' OR muscle_group LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT e.* FROM exercises e JOIN workout_exercises we ON we.exercise_id = e.id GROUP BY e.id ORDER BY COUNT(we.id) DESC")
    fun observeMostUsed(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM workout_exercises WHERE exercise_id = :exerciseId")
    suspend fun usageCount(exerciseId: Long): Int

    @Query("SELECT * FROM exercises")
    suspend fun getAll(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(entities: List<ExerciseEntity>)

    @Query("DELETE FROM exercises")
    suspend fun deleteAll()
}
