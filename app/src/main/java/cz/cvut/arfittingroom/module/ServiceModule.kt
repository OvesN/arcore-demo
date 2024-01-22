package cz.cvut.arfittingroom.module

import cz.cvut.arfittingroom.service.MakeupEditorService
import cz.cvut.arfittingroom.service.MakeupService
import cz.cvut.arfittingroom.service.ModelEditorService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideEditor3DService() = ModelEditorService()

    @Provides
    @Singleton
    fun provideMakeupService() = MakeupService()

    @Provides
    @Singleton
    fun provideMakeupEditorService() = MakeupEditorService()
}