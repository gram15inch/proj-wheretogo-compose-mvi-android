package com.dhkim139.wheretogo.data.di

import android.content.Context
import androidx.room.Room
import com.dhkim139.wheretogo.data.datasource.database.MapDatabase
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
            MapDatabase::class.java,
            "map_db"
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(appDatabase: MapDatabase) = appDatabase.mapDao()
}