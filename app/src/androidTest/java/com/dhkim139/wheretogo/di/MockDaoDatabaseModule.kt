package com.dhkim139.wheretogo.di

import android.content.Context
import androidx.room.Room
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import com.wheretogo.data.di.DaoDatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DaoDatabaseModule::class]
)
class MockDaoDatabaseModule {
    @Provides
    @Singleton
    fun provideCourseDatabase(@ApplicationContext context: Context): CourseDatabase {
        return Room.inMemoryDatabaseBuilder(context, CourseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideCourseDao(database: CourseDatabase) = database.courseDao()

    @Provides
    @Singleton
    fun provideCheckPointDatabase(@ApplicationContext context: Context): CheckPointDatabase {
        return Room.inMemoryDatabaseBuilder(context, CheckPointDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideCheckPointDao(database: CheckPointDatabase) = database.checkPointDao()

}