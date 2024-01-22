package cz.cvut.arfittingroom.component

import cz.cvut.arfittingroom.activity.MakeupActivity
import cz.cvut.arfittingroom.activity.MakeupEditorActivity
import cz.cvut.arfittingroom.activity.ModelEditorActivity
import cz.cvut.arfittingroom.module.ServiceModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ServiceModule::class])
interface AppComponent {
    fun inject(activity: ModelEditorActivity)
    fun inject(activity: MakeupActivity)
    fun inject(activity: MakeupEditorActivity)
}