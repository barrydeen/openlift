package dev.openlift.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.openlift.app.OpenLiftApplication
import dev.openlift.app.di.AppContainer

@Composable
fun appContainer(): AppContainer {
    val context = LocalContext.current
    return (context.applicationContext as OpenLiftApplication).container
}
