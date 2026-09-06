package dev.openlift.app

import android.app.Application
import dev.openlift.app.di.AppContainer

class OpenLiftApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
