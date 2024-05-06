package com.cvut.arfittingroom.component

import com.cvut.arfittingroom.fragment.MakeupEditorFragment
import com.cvut.arfittingroom.activity.ShowRoomActivity
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.module.ServiceModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ServiceModule::class])
interface AppComponent {
    fun inject(activity: ShowRoomActivity)

    fun inject(activity: MakeupEditorFragment)

    fun inject(drawView: DrawView)
}
