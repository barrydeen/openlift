package dev.openlift.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val WORKOUT = "workout/{workoutId}"
    const val PICK_EXERCISE = "pick_exercise?workoutId={workoutId}"
    const val SUMMARY = "summary/{workoutId}"

    fun workout(workoutId: Long) = "workout/$workoutId"
    fun pickExercise(workoutId: Long) = "pick_exercise?workoutId=$workoutId"
    fun summary(workoutId: Long) = "summary/$workoutId"

    val tabRoutes = setOf(HOME, HISTORY, PROGRESS, SETTINGS)
}
