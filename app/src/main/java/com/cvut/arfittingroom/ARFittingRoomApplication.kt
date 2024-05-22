package com.cvut.arfittingroom

import android.app.Application
import com.cvut.arfittingroom.component.AppComponent
import com.cvut.arfittingroom.component.DaggerAppComponent

/**
 * Main application class
 *
 * @author Veronika Ovsyannikova
 */
class ARFittingRoomApplication : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}
