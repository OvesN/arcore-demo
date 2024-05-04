package com.cvut.arfittingroom.module

import com.cvut.arfittingroom.draw.service.LayerManager
import com.cvut.arfittingroom.service.StateService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideStateService() = StateService()

    @Provides
    @Singleton
    fun provideLayerManager() = LayerManager()
}
