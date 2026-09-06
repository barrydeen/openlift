package dev.openlift.app.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.openlift.app.data.ExerciseRepository
import dev.openlift.app.data.WorkoutRepository
import dev.openlift.app.data.local.entity.Equipment
import dev.openlift.app.domain.ExerciseGroup
import dev.openlift.app.domain.PickerOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExercisePickerViewModel(
    private val workoutId: Long,
    private val exercises: ExerciseRepository,
    private val workouts: WorkoutRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value
    }

    val mostUsed: StateFlow<List<dev.openlift.app.data.local.entity.ExerciseEntity>> =
        combine(exercises.observeMostUsed(), _query) { list, q ->
            PickerOrder.filterLongestFirst(q, list)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grouped: StateFlow<List<ExerciseGroup>> =
        combine(exercises.observeAll(), _query) { list, q ->
            PickerOrder.group(PickerOrder.filterLongestFirst(q, list))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _added = MutableStateFlow(false)
    val added: StateFlow<Boolean> = _added.asStateFlow()

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            workouts.addExercise(workoutId, exerciseId)
            _added.value = true
        }
    }

    fun createAndAdd(name: String, muscleGroup: String, equipment: Equipment) {
        viewModelScope.launch {
            val id = exercises.createCustom(name, muscleGroup, equipment)
            workouts.addExercise(workoutId, id)
            _added.value = true
        }
    }

    fun consumeAdded() {
        _added.value = false
    }
}
