package com.wheretogo.data.datasourceimpl


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wheretogo.data.datasource.SettingsLocalDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

private val Context.settingsDataStore by preferencesDataStore(name = "settingDataStore")

class SettingsLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsLocalDataSource {

    override suspend fun mapAppId(): String? =
        context.settingsDataStore.data
            .catch { cause ->
                if (cause is IOException) emit(emptyPreferences()) else throw cause
            }
            .first()[KEY_PREFERRED_MAP_APP]

    override suspend fun setMapAppId(id: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_PREFERRED_MAP_APP] = id
        }
    }

    private companion object {
        val KEY_PREFERRED_MAP_APP = stringPreferencesKey("mapAppId")
    }
}
