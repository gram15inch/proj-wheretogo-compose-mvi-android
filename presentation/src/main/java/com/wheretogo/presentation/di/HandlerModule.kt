package com.wheretogo.presentation.di

import com.wheretogo.domain.handler.CourseAddHandler
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.domain.handler.HomeHandler
import com.wheretogo.domain.handler.LoginHandler
import com.wheretogo.domain.handler.RootHandler
import com.wheretogo.presentation.handler.CourseAddHandlerImpl
import com.wheretogo.presentation.handler.DefaultErrorHandlerImpl
import com.wheretogo.presentation.handler.DriveHandlerImpl
import com.wheretogo.presentation.handler.HomeHandlerImpl
import com.wheretogo.presentation.handler.LoginHandlerImpl
import com.wheretogo.presentation.handler.RootHandlerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class HandlerModule {

    @Provides
    fun provideErrorHandler(): ErrorHandler = DefaultErrorHandlerImpl()

    @Provides
    fun provideHomeHandler(): HomeHandler = HomeHandlerImpl()

    @Provides
    fun provideDriveAddHandler(error: ErrorHandler): DriveHandler = DriveHandlerImpl(error)

    @Provides
    fun provideCourseAddHandler(error: ErrorHandler): CourseAddHandler = CourseAddHandlerImpl(error)

    @Provides
    fun provideLoginHandler(error: ErrorHandler): LoginHandler = LoginHandlerImpl(error)

    @Provides
    fun provideRootHandler(error: ErrorHandler): RootHandler = RootHandlerImpl(error)

}