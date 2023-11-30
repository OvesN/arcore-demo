package cz.cvut.arfittingroom.module

import cz.cvut.arfittingroom.service.ModelEditorService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideEditor3DService() = ModelEditorService()
}