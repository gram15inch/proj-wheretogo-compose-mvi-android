package com.wheretogo.presentation.di

import com.wheretogo.presentation.CourseAddErrorHandler
import com.wheretogo.presentation.DefaultErrorHandler
import com.wheretogo.presentation.ViewModelErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named


@Module
@InstallIn(SingletonComponent::class)
class ErrorModule {

    @Provides
    fun provideDefaultErrorHandler(): ViewModelErrorHandler = DefaultErrorHandler()

    @Named("courseAddError")
    @Provides
    fun provideCourseAddErrorHandler(): ViewModelErrorHandler = CourseAddErrorHandler()
}