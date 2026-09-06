package dev.openlift.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.openlift.app.ui.history.HistoryScreen
import dev.openlift.app.ui.home.HomeScreen
import dev.openlift.app.ui.picker.ExercisePickerScreen
import dev.openlift.app.ui.progress.ProgressScreen
import dev.openlift.app.ui.settings.SettingsScreen
import dev.openlift.app.ui.summary.SummaryScreen
import dev.openlift.app.ui.workout.WorkoutScreen

private data class TabItem(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun OpenLiftApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.tabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(currentRoute = currentRoute, navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) { HomeScreen(navController) }
            composable(Routes.HISTORY) { HistoryScreen(navController) }
            composable(Routes.PROGRESS) { ProgressScreen(navController) }
            composable(Routes.SETTINGS) { SettingsScreen(navController) }

            composable(
                route = Routes.WORKOUT,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { entry ->
                val workoutId = entry.arguments?.getLong("workoutId") ?: return@composable
                WorkoutScreen(navController, workoutId)
            }

            composable(
                route = Routes.PICK_EXERCISE,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { entry ->
                val workoutId = entry.arguments?.getLong("workoutId") ?: return@composable
                ExercisePickerScreen(navController, workoutId)
            }

            composable(
                route = Routes.SUMMARY,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { entry ->
                val workoutId = entry.arguments?.getLong("workoutId") ?: return@composable
                SummaryScreen(navController, workoutId)
            }
        }
    }
}

@Composable
private fun AppBottomBar(currentRoute: String?, navController: NavHostController) {
    val tabs = listOf(
        TabItem(Routes.HOME, "Home", { Icon(Icons.Filled.FitnessCenter, contentDescription = null) }),
        TabItem(Routes.HISTORY, "History", { Icon(Icons.Filled.History, contentDescription = null) }),
        TabItem(Routes.PROGRESS, "Progress", { Icon(Icons.Filled.BarChart, contentDescription = null) }),
        TabItem(Routes.SETTINGS, "Settings", { Icon(Icons.Filled.Settings, contentDescription = null) })
    )
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = tab.icon,
                label = { Text(tab.label) }
            )
        }
    }
}
