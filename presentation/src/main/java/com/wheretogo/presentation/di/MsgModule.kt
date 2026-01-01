package com.wheretogo.presentation.di

import com.wheretogo.presentation.HomeViewModelEventHandler
import com.wheretogo.presentation.ViewModelEventHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class MsgModule {

    @Provides
    fun provideDefaultMsgHandler(): ViewModelEventHandler = HomeViewModelEventHandler()

}