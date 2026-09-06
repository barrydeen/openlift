package dev.openlift.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.openlift.app.data.SettingsRepository
import dev.openlift.app.data.WorkoutRepository
import dev.openlift.app.data.local.entity.WorkoutWithExercises
import dev.openlift.app.domain.WeightUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SummaryViewModel(
    private val workoutId: Long,
    private val workouts: WorkoutRepository,
    settings: SettingsRepository
) : ViewModel() {

    val workout: StateFlow<WorkoutWithExercises?> =
        workouts.observeWorkout(workoutId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unit: StateFlow<WeightUnit> =
        settings.unit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    fun deleteWorkout(onDeleted: () -> Unit) {
        viewModelScope.launch {
            workouts.deleteWorkout(workoutId)
            onDeleted()
        }
    }
}
