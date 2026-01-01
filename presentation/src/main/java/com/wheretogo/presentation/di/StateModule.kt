package com.wheretogo.presentation.di

import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.HomeScreenState
import com.wheretogo.presentation.state.SearchBarState
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
    fun provideDriveState(): DriveScreenState {
        return DriveScreenState(
            searchBarState = SearchBarState(
                isAdVisible = true
            )
        )
    }
}