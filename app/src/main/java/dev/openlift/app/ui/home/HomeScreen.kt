package dev.openlift.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.openlift.app.domain.TimeFormatter
import dev.openlift.app.domain.UnitConverter
import dev.openlift.app.ui.appContainer
import dev.openlift.app.ui.components.EmptyState
import dev.openlift.app.ui.components.SectionHeader
import dev.openlift.app.ui.navigation.Routes
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val container = appContainer()
    val vm: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HomeViewModel(container.workoutRepository, container.settingsRepository) }
        }
    )

    val activeWorkout by vm.activeWorkout.collectAsState()
    val history by vm.history.collectAsState()
    val unit by vm.unit.collectAsState()
    val launchedId by vm.launchedWorkoutId.collectAsState()
    var showNewWorkout by remember { mutableStateOf(false) }

    LaunchedEffect(launchedId) {
        if (launchedId != null) {
            navController.navigate(Routes.workout(launchedId!!))
            vm.consumeLaunch()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("OpenLift") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    SectionHeader("Today")
                    if (activeWorkout != null) {
                        ActiveWorkoutCard(
                            name = activeWorkout!!.name,
                            startedAt = activeWorkout!!.startedAt,
                            onClick = { navController.navigate(Routes.workout(activeWorkout!!.id)) }
                        )
                    } else {
                        Button(
                            onClick = { showNewWorkout = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Workout")
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showNewWorkout = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("New Workout")
                }
            }

            item { SectionHeader("Recent Sessions") }

            if (history.isEmpty()) {
                item {
                    EmptyState(
                        title = "No sessions yet",
                        subtitle = "Create a workout and log your first lifts to see them here."
                    )
                }
            } else {
                items(history, key = { it.workoutId }) { row ->
                    SessionCard(
                        name = row.name,
                        dateText = TimeFormatter.date(row.startedAt),
                        volumeText = "${UnitConverter.displayWeight(row.totalKg, unit)} ${unit.label}",
                        exerciseCount = row.exerciseCount,
                        setCount = row.setCount,
                        durationText = TimeFormatter.duration(row.startedAt, row.endedAt),
                        onClick = { navController.navigate(Routes.summary(row.workoutId)) }
                    )
                }
            }
        }
    }

    if (showNewWorkout) {
        NewWorkoutDialog(
            onConfirm = { name ->
                vm.startWorkout(name)
                showNewWorkout = false
            },
            onDismiss = { showNewWorkout = false }
        )
    }
}

@Composable
private fun ActiveWorkoutCard(name: String, startedAt: Long, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "ACTIVE",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Started ${TimeFormatter.dateTime(startedAt)}",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SessionCard(
    name: String,
    dateText: String,
    volumeText: String,
    exerciseCount: Int,
    setCount: Int,
    durationText: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = volumeText,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(dateText, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                Text("$exerciseCount exercises", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                Text("$setCount sets", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                Text(durationText, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun NewWorkoutDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Workout") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Workout name") },
                placeholder = { Text("e.g. Push Day") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) { Text("Start") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
