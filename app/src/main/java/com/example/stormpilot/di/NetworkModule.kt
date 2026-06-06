package com.example.stormpilot.di

import com.example.stormpilot.features.shared.data.api.HttpStormPilotApi
import com.example.stormpilot.features.shared.data.api.StormPilotApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    @Singleton
    abstract fun bindStormPilotApi(impl: HttpStormPilotApi): StormPilotApi
}
