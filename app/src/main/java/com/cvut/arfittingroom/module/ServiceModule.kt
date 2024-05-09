package com.cvut.arfittingroom.module

import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.CirclePathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.HeartPathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.RectanglePathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.StarPathCreationStrategy
import com.cvut.arfittingroom.draw.service.LayerManager
import com.cvut.arfittingroom.service.Mapper
import com.cvut.arfittingroom.service.StateService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import javax.inject.Inject
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
    fun provideMapper(strategies: Map<String, @JvmSuppressWildcards PathCreationStrategy>): Mapper {
        return Mapper(strategies)
    }
}

