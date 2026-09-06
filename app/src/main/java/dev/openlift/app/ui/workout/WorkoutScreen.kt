package dev.openlift.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import dev.openlift.app.data.local.entity.WorkoutExerciseWithRelation
import dev.openlift.app.data.local.entity.WorkoutSetEntity
import dev.openlift.app.domain.TimeFormatter
import dev.openlift.app.domain.WeightUnit
import dev.openlift.app.domain.UnitConverter
import dev.openlift.app.ui.appContainer
import dev.openlift.app.ui.components.EmptyState
import dev.openlift.app.ui.components.SectionHeader
import dev.openlift.app.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(navController: NavHostController, workoutId: Long) {
    val container = appContainer()
    val vm: WorkoutViewModel = viewModel(
        factory = viewModelFactory {
            initializer { WorkoutViewModel(workoutId, container.workoutRepository, container.settingsRepository) }
        }
    )

    val workout by vm.workout.collectAsState()
    val unit by vm.unit.collectAsState()
    val restRemaining by vm.restRemaining.collectAsState()
    val restRunning by vm.restRunning.collectAsState()
    var showFinish by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout?.workout?.name ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showFinish = true }) { Text("Finish") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (restRunning) {
                RestTimerBar(remaining = restRemaining, onSkip = { vm.skipRest() })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (workout == null) {
                    item { EmptyState("Loading", "Preparing your workout…") }
                } else if (workout!!.exercises.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No exercises yet",
                            subtitle = "Add an exercise to start logging your sets."
                        )
                    }
                    item { AddExerciseButton { navController.navigate(Routes.pickExercise(workoutId)) } }
                } else {
                    items(workout!!.exercises, key = { it.entry.id }) { entry ->
                        ExerciseCard(
                            entry = entry,
                            unit = unit,
                            onAddDraft = { weightKg, reps, warmup -> vm.addDraftSet(entry.entry.id, weightKg, reps, warmup) },
                            onComplete = { vm.completeSet(it) },
                            onUpdate = { id, wkg, reps -> vm.updateSet(id, wkg, reps) },
                            onToggleWarmup = { vm.toggleWarmup(it) },
                            onDeleteSet = { vm.deleteSet(it) },
                            onRemove = { vm.removeExercise(entry.entry.id) }
                        )
                    }
                    item { AddExerciseButton { navController.navigate(Routes.pickExercise(workoutId)) } }
                }
            }
        }
    }

    if (showFinish) {
        AlertDialog(
            onDismissRequest = { showFinish = false },
            title = { Text("Finish workout?") },
            text = { Text("You're about to end this session. Make sure all your sets are logged.") },
            confirmButton = {
                TextButton(onClick = {
                    showFinish = false
                    vm.finishWorkout {
                        navController.navigate(Routes.summary(workoutId)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                }) { Text("Finish") }
            },
            dismissButton = {
                TextButton(onClick = { showFinish = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RestTimerBar(remaining: Int, onSkip: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Rest ${TimeFormatter.countdown(remaining)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onSkip) {
                Icon(Icons.Filled.TimerOff, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Skip")
            }
        }
    }
}

@Composable
private fun AddExerciseButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add Exercise")
    }
}

@Composable
private fun ExerciseCard(
    entry: WorkoutExerciseWithRelation,
    unit: WeightUnit,
    onAddDraft: (Double, Int, Boolean) -> Unit,
    onComplete: (Long) -> Unit,
    onUpdate: (Long, Double, Int) -> Unit,
    onToggleWarmup: (Long) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.exercise?.name ?: "Exercise",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove exercise")
                }
            }
            if (entry.exercise != null) {
                Text(
                    text = "${entry.exercise.muscleGroup} · ${entry.exercise.equipment}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))

            val sets = entry.sets.sortedBy { it.position }
            sets.forEachIndexed { index, set ->
                if (set.completedAt == null) {
                    DraftSetRow(
                        set = set,
                        index = index,
                        unit = unit,
                        onUpdate = onUpdate,
                        onComplete = onComplete,
                        onDelete = onDeleteSet
                    )
                } else {
                    CompletedSetRow(
                        set = set,
                        index = index,
                        unit = unit,
                        onToggleWarmup = onToggleWarmup,
                        onDelete = onDeleteSet
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            val last = sets.lastOrNull()
            AddSetRow(
                unit = unit,
                defaultWeightKg = last?.weightKg ?: 0.0,
                defaultReps = last?.reps ?: 0,
                onAdd = { weightKg, reps -> onAddDraft(weightKg, reps, false) }
            )
        }
    }
}

@Composable
private fun DraftSetRow(
    set: WorkoutSetEntity,
    index: Int,
    unit: WeightUnit,
    onUpdate: (Long, Double, Int) -> Unit,
    onComplete: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    var weightText by remember(set.id) { mutableStateOf(UnitConverter.displayWeight(set.weightKg, unit)) }
    var repsText by remember(set.id) { mutableStateOf(set.reps.toString()) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${set.position + 1}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(24.dp)
        )
        NumberField(
            value = weightText,
            onValueChange = { weightText = it },
            suffix = unit.label,
            modifier = Modifier.weight(1f)
        )
        Text("×", modifier = Modifier.padding(horizontal = 6.dp))
        NumberField(
            value = repsText,
            onValueChange = { repsText = it },
            suffix = "reps",
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            val w = UnitConverter.parseWeight(weightText, unit) ?: set.weightKg
            val r = repsText.toIntOrNull() ?: set.reps
            onUpdate(set.id, w, r)
            onComplete(set.id)
        }) {
            Icon(Icons.Filled.Check, contentDescription = "Log set", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = { onDelete(set.id) }) {
            Icon(Icons.Filled.Close, contentDescription = "Discard set")
        }
    }
}

@Composable
private fun CompletedSetRow(
    set: WorkoutSetEntity,
    index: Int,
    unit: WeightUnit,
    onToggleWarmup: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${set.position + 1}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = "${UnitConverter.displayWeight(set.weightKg, unit)} ${unit.label} × ${set.reps}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (set.isWarmup) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "warmup",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        IconButton(onClick = { onToggleWarmup(set.id) }) {
            Icon(Icons.Filled.Schedule, contentDescription = "Mark warmup")
        }
        IconButton(onClick = { onDelete(set.id) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete set")
        }
    }
}

@Composable
private fun AddSetRow(
    unit: WeightUnit,
    defaultWeightKg: Double,
    defaultReps: Int,
    onAdd: (Double, Int) -> Unit
) {
    var weightText by remember(defaultWeightKg, defaultReps) {
        mutableStateOf(UnitConverter.displayWeight(defaultWeightKg, unit))
    }
    var repsText by remember(defaultWeightKg, defaultReps) {
        mutableStateOf(if (defaultReps > 0) defaultReps.toString() else "")
    }

    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            NumberField(
                value = weightText,
                onValueChange = { weightText = it },
                suffix = unit.label,
                modifier = Modifier.weight(1f)
            )
            Text("×", modifier = Modifier.padding(horizontal = 6.dp))
            NumberField(
                value = repsText,
                onValueChange = { repsText = it },
                suffix = "reps",
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = {
                val w = UnitConverter.parseWeight(weightText, unit) ?: defaultWeightKg
                val r = repsText.toIntOrNull() ?: continueWithReps(defaultReps)
                onAdd(w, r)
            }) { Text("Add") }
        }
    }
}

private fun continueWithReps(reps: Int): Int = if (reps > 0) reps else 10

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = { Text(suffix, style = MaterialTheme.typography.labelSmall) }
    )
}
