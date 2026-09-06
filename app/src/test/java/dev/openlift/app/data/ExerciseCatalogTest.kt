package dev.openlift.app.data

import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseCatalogTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `toEntities deduplicates case-insensitively`() {
        val dtos = listOf(
            ExerciseCatalogDto("Bench Press", "Chest", "BARBELL"),
            ExerciseCatalogDto("bench press", "Chest", "BARBELL")
        )
        val entities = ExerciseCatalog.toEntities(dtos)
        assertTrue(entities.size == 1)
    }

    @Test
    fun `catalog asset is valid and contains both barbell and dumbbell`() {
        val assetFile = File("src/main/assets/exercises.json")
        assertTrue("exercises.json asset must exist", assetFile.exists())
        val text = assetFile.readText()
        val dtos = json.decodeFromString<List<ExerciseCatalogDto>>(text)

        assertTrue("catalog should be non-empty", dtos.isNotEmpty())
        val differentlyNamed = dtos.map { it.name.lowercase() }.distinct()
        assertTrue("names should be unique", differentlyNamed.size == dtos.size)

        val equipment = dtos.map { it.equipment }.toSet()
        assertTrue("catalog should include barbell exercises", "BARBELL" in equipment)
        assertTrue("catalog should include dumbbell exercises", "DUMBBELL" in equipment)

        // Assert broad coverage of muscle groups.
        val groups = dtos.map { it.muscleGroup }.toSet()
        assertTrue("Chest" in groups)
        assertTrue("Back" in groups)
        assertTrue("Shoulders" in groups)
        assertTrue("Quads" in groups)
    }
}
