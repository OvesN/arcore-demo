package cz.cvut.arfittingroom

import android.app.Application
import cz.cvut.arfittingroom.component.AppComponent
import cz.cvut.arfittingroom.component.DaggerAppComponent

class ARFittingRoomApplication() : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}