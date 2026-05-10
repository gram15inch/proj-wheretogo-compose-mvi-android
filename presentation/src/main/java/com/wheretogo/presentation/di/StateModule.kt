package com.wheretogo.presentation.di

import com.wheretogo.presentation.PresentationBuildConfig
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.HomeScreenState
import com.wheretogo.presentation.state.MapState
import com.wheretogo.presentation.state.NaverMapState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StateModule {

    @Singleton
    @Provides
    fun provideHomeState(): HomeScreenState {
        return HomeScreenState()
    }

    @Singleton
    @Provides
    fun provideDriveState(
        buildConfig: PresentationBuildConfig
    ): DriveScreenState {
        return DriveScreenState(
            isTestUi = buildConfig.isTestUi
        )
    }

    @Singleton
    @Provides
    fun provideCourseAddState(
        buildConfig: PresentationBuildConfig
    ): CourseAddScreenState {
        return CourseAddScreenState(
            naverMapState = NaverMapState(isZoomControl = buildConfig.isTestUi),
            isTestUi = buildConfig.isTestUi
        )
    }

    @Singleton
    @Provides
    fun provideMapState(
        buildConfig: PresentationBuildConfig
    ): MapState {
        return MapState(
            naverMapState = NaverMapState(isZoomControl = buildConfig.isTestUi),
        )
    }
}