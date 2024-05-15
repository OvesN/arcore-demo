package com.cvut.arfittingroom.module

import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.CirclePathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.HeartPathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.RectanglePathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.StarPathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.TrianglePathCreationStrategy
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class BindingModule {
    @Binds
    @IntoMap
    @StringKey("circle")
    abstract fun bindCirclePathCreationStrategy(impl: CirclePathCreationStrategy): PathCreationStrategy

    @Binds
    @IntoMap
    @StringKey("rectangle")
    abstract fun bindRectanglePathCreationStrategy(impl: RectanglePathCreationStrategy): PathCreationStrategy

    @Binds
    @IntoMap
    @StringKey("star")
    abstract fun bindStarPathCreationStrategy(impl: StarPathCreationStrategy): PathCreationStrategy

    @Binds
    @IntoMap
    @StringKey("heart")
    abstract fun bindHeartPathCreationStrategy(impl: HeartPathCreationStrategy): PathCreationStrategy

    @Binds
    @IntoMap
    @StringKey("triangle")
    abstract fun bindTrianglePathCreationStrategy(impl: TrianglePathCreationStrategy): PathCreationStrategy
}
