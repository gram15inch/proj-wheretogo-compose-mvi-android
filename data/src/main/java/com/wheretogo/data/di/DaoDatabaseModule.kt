package com.wheretogo.data.di

import android.content.Context
import androidx.room.Room
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
    fun provideCourseDatabase(@ApplicationContext context: Context): CourseDatabase {
        return Room.databaseBuilder(
            context,
            CourseDatabase::class.java,
            "course_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCourseDao(database: CourseDatabase) = database.courseDao()

    @Provides
    @Singleton
    fun provideCheckPointDatabase(@ApplicationContext context: Context): CheckPointDatabase {
        return Room.databaseBuilder(
            context,
            CheckPointDatabase::class.java,
            "checkpoint_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCheckPointDao(database: CheckPointDatabase) = database.checkPointDao()

}