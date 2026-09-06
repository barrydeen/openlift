package dev.openlift.app.data

import dev.openlift.app.data.local.dao.ExerciseDao
import dev.openlift.app.data.local.entity.ExerciseEntity
import dev.openlift.app.data.local.entity.Equipment
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val dao: ExerciseDao) {

    fun observeAll(): Flow<List<ExerciseEntity>> = dao.observeAll()

    fun observeMostUsed(): Flow<List<ExerciseEntity>> = dao.observeMostUsed()

    fun search(query: String): Flow<List<ExerciseEntity>> = dao.search(query)

    suspend fun getById(id: Long): ExerciseEntity? = dao.getById(id)

    suspend fun usageCount(id: Long): Int = dao.usageCount(id)

    suspend fun createCustom(name: String, muscleGroup: String, equipment: Equipment): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Exercise name must not be empty" }
        val group = muscleGroup.trim().ifEmpty { "Other" }
        return dao.insert(
            ExerciseEntity(name = trimmed, muscleGroup = group, equipment = equipment.name, isCustom = true)
        )
    }
}
