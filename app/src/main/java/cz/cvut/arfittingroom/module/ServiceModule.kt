package cz.cvut.arfittingroom.module

import cz.cvut.arfittingroom.service.Editor3DService
import dagger.Module
import dagger.Provides

@Module
class ServiceModule {
    @Provides
    fun provideEditor3DService() = Editor3DService()
}