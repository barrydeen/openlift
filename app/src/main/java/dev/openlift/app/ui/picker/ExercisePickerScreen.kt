package dev.openlift.app.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.navigation.NavHostController
import dev.openlift.app.data.local.entity.Equipment
import dev.openlift.app.ui.appContainer
import dev.openlift.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(navController: NavHostController, workoutId: Long) {
    val container = appContainer()
    val vm: ExercisePickerViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ExercisePickerViewModel(workoutId, container.exerciseRepository, container.workoutRepository)
            }
        }
    )

    val query by vm.query.collectAsState()
    val mostUsed by vm.mostUsed.collectAsState()
    val grouped by vm.grouped.collectAsState()
    val added by vm.added.collectAsState()
    var showCustom by remember { mutableStateOf(false) }

    LaunchedEffect(added) {
        if (added) {
            navController.popBackStack()
            vm.consumeAdded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Exercise") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showCustom = true }) { Text("New") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.setQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Search exercises") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (query.isBlank() && mostUsed.isNotEmpty()) {
                    item {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            SectionHeader("Most used")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(mostUsed) { exercise ->
                                    AssistChip(
                                        onClick = { vm.addExercise(exercise.id) },
                                        label = { Text(exercise.name) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item { Spacer(Modifier.width(8.dp)) }
                }

                grouped.forEach { group ->
                    item {
                        Spacer(Modifier.padding(top = 8.dp))
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            SectionHeader(group.muscleGroup)
                        }
                    }
                    items(group.exercises, key = { it.id }) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = {
                                Text(
                                    "${exercise.muscleGroup} · ${exercise.equipment}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Icon(Icons.Filled.Add, contentDescription = "Add exercise")
                            },
                            modifier = Modifier.clickable { vm.addExercise(exercise.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCustom) {
        CustomExerciseDialog(
            onConfirm = { name, group, equipment -> vm.createAndAdd(name, group, equipment) },
            onDismiss = { showCustom = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomExerciseDialog(
    onConfirm: (String, String, Equipment) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("Other") }
    var equipment by remember { mutableStateOf(Equipment.DUMBBELL) }
    var groupExpanded by remember { mutableStateOf(false) }

    val groups = listOf(
        "Chest", "Back", "Shoulders", "Quads", "Hamstrings", "Glutes",
        "Calves", "Biceps", "Triceps", "Forearms", "Core", "Full Body", "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = { groupExpanded = it }
                ) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Muscle group") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false }
                    ) {
                        groups.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    group = g
                                    groupExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                EquipmentSelector(
                    selected = equipment,
                    onSelect = { equipment = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, group, equipment) }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentSelector(selected: Equipment, onSelect: (Equipment) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Equipment.entries.forEach { eq ->
            RetainingAssistChip(label = eq.name, selected = eq == selected, onClick = { onSelect(eq) })
        }
    }
}

@Composable
private fun RetainingAssistChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) }
    )
}
