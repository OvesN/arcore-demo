package com.cvut.arfittingroom

import android.app.Application
import com.cvut.arfittingroom.component.AppComponent
import com.cvut.arfittingroom.component.DaggerAppComponent

class ARFittingRoomApplication : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = com.cvut.arfittingroom.component.DaggerAppComponent.create()
    }
}
