package com.wheretogo.data.di

import android.content.Context
import androidx.room.Room
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import com.wheretogo.data.datasourceimpl.database.ReportDatabase
import com.wheretogo.data.datasourceimpl.database.RouteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 수정시 테스트를 위해 MockDaoDatabaseModule 과 맞춤
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
    @Singleton
    fun provideReportDatabase(@ApplicationContext context: Context): ReportDatabase {
        return Room.databaseBuilder(
            context,
            ReportDatabase::class.java,
            "report_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideRouteDatabase(@ApplicationContext context: Context): RouteDatabase {
        return Room.databaseBuilder(
            context,
            RouteDatabase::class.java,
            "route_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

}