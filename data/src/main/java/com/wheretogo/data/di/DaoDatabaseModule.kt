package com.wheretogo.data.di

import android.content.Context
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoDatabaseModule {

    @Provides
    @Singleton
    fun provideCourseDatabase(@ApplicationContext context: Context) =
        CourseDatabase.getInstance(context)

    @Provides
    fun provideCourseDao(database: CourseDatabase) = database.courseDao()

    @Provides
    @Singleton
    fun provideCheckPointDatabase(@ApplicationContext context: Context) =
        CheckPointDatabase.getInstance(context)

    @Provides
    fun provideCheckPointDao(database: CheckPointDatabase) = database.checkPointDao()

}