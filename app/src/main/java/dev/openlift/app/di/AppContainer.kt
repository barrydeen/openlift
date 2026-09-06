package dev.openlift.app.di

import android.content.Context
import dev.openlift.app.data.BackupManager
import dev.openlift.app.data.ExerciseRepository
import dev.openlift.app.data.ExerciseSeeder
import dev.openlift.app.data.SettingsRepository
import dev.openlift.app.data.WorkoutRepository
import dev.openlift.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase = AppDatabase.build(appContext)

    val exerciseRepository = ExerciseRepository(database.exerciseDao())

    val workoutRepository = WorkoutRepository(database.workoutDao(), database.exerciseDao())

    val settingsRepository = SettingsRepository(appContext)

    val backupManager = BackupManager(appContext, database)

    init {
        ExerciseSeeder(applicationScope, appContext, database).seedIfNeeded()
    }
}
