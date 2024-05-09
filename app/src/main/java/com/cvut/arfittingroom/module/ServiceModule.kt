package com.cvut.arfittingroom.module

import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.service.LayerManager
import com.cvut.arfittingroom.service.Mapper
import com.cvut.arfittingroom.service.StateService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideStateService(): StateService = StateService()

    @Provides
    @Singleton
    fun provideLayerManager(): LayerManager = LayerManager()

    @Provides
    @Singleton
    fun provideMapper(strategies: Map<String, @JvmSuppressWildcards PathCreationStrategy>): Mapper = Mapper(strategies)
}
