package com.wheretogo.data.di

import android.content.Context
import androidx.room.Room
import com.wheretogo.data.datasource.database.JourneyDatabase
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
    fun provideMapDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            JourneyDatabase::class.java,
            "journey_db"
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideJourneyDao(database: JourneyDatabase) = database.journeyDao()
}