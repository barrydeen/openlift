package dev.openlift.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.openlift.app.data.SettingsRepository
import dev.openlift.app.data.WorkoutRepository
import dev.openlift.app.data.local.entity.WorkoutWithExercises
import dev.openlift.app.domain.WeightUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutId: Long,
    private val workouts: WorkoutRepository,
    settings: SettingsRepository
) : ViewModel() {

    val workout: StateFlow<WorkoutWithExercises?> =
        workouts.observeWorkout(workoutId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unit: StateFlow<WeightUnit> =
        settings.unit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    val defaultRestSeconds: StateFlow<Long> =
        settings.restSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_REST_SECONDS)

    private val _sessionRestOverride = MutableStateFlow<Long?>(null)
    val sessionRestSeconds: StateFlow<Long> =
        combine(defaultRestSeconds, _sessionRestOverride) { default, override -> override ?: default }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_REST_SECONDS)

    // Rest timer ------------------------------------------------
    private var restJob: Job? = null
    private val _restRemaining = MutableStateFlow(0)
    val restRemaining: StateFlow<Int> = _restRemaining.asStateFlow()
    private val _restRunning = MutableStateFlow(false)
    val restRunning: StateFlow<Boolean> = _restRunning.asStateFlow()

    fun startRest() {
        startRest(sessionRestSeconds.value)
    }

    fun startRest(seconds: Long) {
        restJob?.cancel()
        _restRunning.value = true
        val total = seconds.toInt()
        _restRemaining.value = total
        restJob = viewModelScope.launch {
            var remaining = total
            while (remaining > 0) {
                delay(1000)
                remaining--
                _restRemaining.value = remaining
            }
            stopRest()
        }
    }

    fun skipRest() {
        restJob?.cancel()
        stopRest()
    }

    private fun stopRest() {
        restJob = null
        _restRunning.value = false
        _restRemaining.value = 0
    }

    // Set actions ------------------------------------------------
    fun addSet(workoutExerciseId: Long, weightKg: Double, reps: Int, isWarmup: Boolean = false) {
        viewModelScope.launch {
            workouts.addSet(workoutExerciseId, weightKg, reps, isWarmup)
            if (!isWarmup) startRest()
        }
    }

    fun addDraftSet(workoutExerciseId: Long, weightKg: Double, reps: Int, isWarmup: Boolean = false) {
        viewModelScope.launch {
            workouts.addDraftSet(workoutExerciseId, weightKg, reps, isWarmup)
        }
    }

    fun completeSet(setId: Long) {
        viewModelScope.launch {
            workouts.completeSet(setId)
            startRest()
        }
    }

    fun updateSet(setId: Long, weightKg: Double, reps: Int) {
        viewModelScope.launch { workouts.updateSet(setId, weightKg, reps) }
    }

    fun toggleWarmup(setId: Long) {
        viewModelScope.launch { workouts.toggleWarmup(setId) }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { workouts.deleteSet(setId) }
    }

    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch { workouts.removeExercise(workoutExerciseId) }
    }

    // Finish -----------------------------------------------------
    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun finishWorkout(onFinished: () -> Unit) {
        viewModelScope.launch {
            workouts.finishWorkout(workoutId)
            _finished.value = true
            onFinished()
        }
    }
}
