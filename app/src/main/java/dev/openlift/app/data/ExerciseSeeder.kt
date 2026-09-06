package dev.openlift.app.data

import android.content.Context
import dev.openlift.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ExerciseSeeder(
    private val scope: CoroutineScope,
    private val context: Context,
    private val database: AppDatabase
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun seedIfNeeded() {
        scope.launch {
            if (database.exerciseDao().count() > 0) return@launch
            runCatching { seed() }
        }
    }

    internal suspend fun seed() {
        val text = context.assets.open(ExerciseCatalog.ASSET).bufferedReader().use { it.readText() }
        val dtos = json.decodeFromString<List<ExerciseCatalogDto>>(text)
        database.exerciseDao().insertAll(ExerciseCatalog.toEntities(dtos))
    }
}
