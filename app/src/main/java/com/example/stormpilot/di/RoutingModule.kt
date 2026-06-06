package com.example.stormpilot.di

import com.example.stormpilot.features.shared.data.routing.RoutingRepository
import com.example.stormpilot.features.shared.data.routing.RoutingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutingModule {
    @Binds
    @Singleton
    abstract fun bindRoutingRepository(impl: RoutingRepositoryImpl): RoutingRepository
}
