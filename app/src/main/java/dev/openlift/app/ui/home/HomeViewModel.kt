package dev.openlift.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.openlift.app.data.SettingsRepository
import dev.openlift.app.data.WorkoutRepository
import dev.openlift.app.data.local.entity.WorkoutEntity
import dev.openlift.app.data.local.entity.WorkoutSummaryRow
import dev.openlift.app.domain.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val workouts: WorkoutRepository,
    settings: SettingsRepository
) : ViewModel() {

    val activeWorkout: StateFlow<WorkoutEntity?> =
        workouts.observeActiveWorkout().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val history: StateFlow<List<WorkoutSummaryRow>> =
        workouts.observeHistorySummaries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unit: StateFlow<WeightUnit> =
        settings.unit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    private val _launchedWorkoutId = MutableStateFlow<Long?>(null)
    val launchedWorkoutId: StateFlow<Long?> = _launchedWorkoutId.asStateFlow()

    fun startWorkout(name: String) {
        viewModelScope.launch {
            _launchedWorkoutId.value = workouts.createWorkout(name)
        }
    }

    fun consumeLaunch() {
        _launchedWorkoutId.value = null
    }
}
