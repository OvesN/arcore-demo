package com.cvut.arfittingroom.component

import com.cvut.arfittingroom.activity.MakeupEditorActivity
import com.cvut.arfittingroom.activity.ShowRoomActivity
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.module.ServiceModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ServiceModule::class])
interface AppComponent {
    fun inject(activity: ShowRoomActivity)

    fun inject(activity: MakeupEditorActivity)

    fun inject(drawView: DrawView)
}
