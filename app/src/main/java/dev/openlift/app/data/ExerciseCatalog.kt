package dev.openlift.app.data

import dev.openlift.app.data.local.entity.ExerciseEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseCatalogDto(
    val name: String,
    @SerialName("muscle_group") val muscleGroup: String,
    val equipment: String
) {
    fun toEntity(): ExerciseEntity = ExerciseEntity(
        name = name,
        muscleGroup = muscleGroup,
        equipment = equipment,
        isCustom = false
    )
}

object ExerciseCatalog {

    const val ASSET = "exercises.json"

    fun toEntities(dtos: List<ExerciseCatalogDto>): List<ExerciseEntity> =
        dtos.distinctBy { it.name.lowercase() }.map { it.toEntity() }
}
