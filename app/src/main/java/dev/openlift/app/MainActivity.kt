package dev.openlift.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.openlift.app.ui.navigation.OpenLiftApp
import dev.openlift.app.ui.theme.OpenLiftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenLiftTheme {
                OpenLiftApp()
            }
        }
    }
}
