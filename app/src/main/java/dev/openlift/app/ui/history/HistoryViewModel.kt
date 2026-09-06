package dev.openlift.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.openlift.app.data.WorkoutRepository
import dev.openlift.app.data.local.entity.WorkoutSummaryRow
import dev.openlift.app.domain.WeightUnit
import dev.openlift.app.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val workouts: WorkoutRepository,
    settings: SettingsRepository
) : ViewModel() {

    val history: StateFlow<List<WorkoutSummaryRow>> =
        workouts.observeHistorySummaries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unit: StateFlow<WeightUnit> =
        settings.unit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    fun deleteWorkout(id: Long) {
        viewModelScope.launch { workouts.deleteWorkout(id) }
    }
}
