package dev.openlift.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.openlift.app.data.ExerciseRepository
import dev.openlift.app.data.SettingsRepository
import dev.openlift.app.data.WorkoutRepository
import dev.openlift.app.data.local.entity.ExerciseEntity
import dev.openlift.app.domain.E1RmPoint
import dev.openlift.app.domain.ProgressCalculator
import dev.openlift.app.domain.WeightUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(
    private val exercisesRepo: ExerciseRepository,
    private val workouts: WorkoutRepository,
    settings: SettingsRepository
) : ViewModel() {

    val unit: StateFlow<WeightUnit> =
        settings.unit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    val exercises: StateFlow<List<ExerciseEntity>> =
        exercisesRepo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedId = MutableStateFlow<Long?>(null)
    val selectedId: StateFlow<Long?> = _selectedId.asStateFlow()

    val points: StateFlow<List<E1RmPoint>> = _selectedId.flatMapLatest { id ->
        if (id == null) flow { emit(emptyList()) }
        else flow { emit(ProgressCalculator.sessionPoints(workouts.setsExerciseHistory(id))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun select(id: Long) {
        _selectedId.value = id
    }

    fun clearSelection() {
        _selectedId.value = null
    }
}
