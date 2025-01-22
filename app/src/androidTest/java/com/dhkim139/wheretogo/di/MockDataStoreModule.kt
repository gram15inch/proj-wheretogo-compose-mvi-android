package com.dhkim139.wheretogo.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.wheretogo.data.di.DataStoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.io.File
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class]
)
class MockDataStoreModule {

    @Provides
    @Singleton
    fun provideUserDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            File.createTempFile("test_user_datastore", ".preferences_pb")
        }
    }

    @Provides
    @Singleton
    fun provideImageFile(): File {
        return File.createTempFile("test", ".jpg_pb")
    }
}