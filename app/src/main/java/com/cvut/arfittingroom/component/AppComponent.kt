package com.cvut.arfittingroom.component

import com.cvut.arfittingroom.activity.FittingRoomActivity
import com.cvut.arfittingroom.draw.DrawView
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.fragment.MaskEditorFragment
import com.cvut.arfittingroom.module.BindingModule
import com.cvut.arfittingroom.module.ServiceModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ServiceModule::class, BindingModule::class])
interface AppComponent {
    fun inject(activity: FittingRoomActivity)

    fun inject(activity: MaskEditorFragment)

    fun inject(drawView: DrawView)

    fun getPathCreationStrategies(): Map<String, @JvmSuppressWildcards PathCreationStrategy>
}
