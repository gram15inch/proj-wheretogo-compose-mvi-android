package com.wheretogo.data.datasourceimpl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.wheretogo.data.DataError
import com.wheretogo.data.DataSettingAttr
import com.wheretogo.data.datasource.AppLocalDatasource
import com.wheretogo.data.feature.SecureStore
import com.wheretogo.data.feature.dataErrorCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class AppLocalDatasourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("appDataStore") private val dataStore: DataStore<Preferences>,
    @Named("appPref") private val prefs: SharedPreferences,
    private val secureStore: SecureStore
) : AppLocalDatasource {
    private val tutorialKey = intPreferencesKey(DataSettingAttr.TUTORIAL.name)
    private val prefsLastAppVersion = "lastAppVersion"

    override suspend fun observeInt(key: DataSettingAttr): Flow<Int> {
        return when (key) {
            DataSettingAttr.TUTORIAL -> {
                dataStore.data.map { preferences ->
                    preferences[tutorialKey] ?: 0
                }
            }
        }
    }

    override suspend fun getInt(key: DataSettingAttr): Result<Int> {
        return dataErrorCatching {
            when (key) {
                DataSettingAttr.TUTORIAL -> {
                    dataStore.data.map { preferences ->
                        preferences[tutorialKey] ?: 0
                    }.first()
                }
            }
        }
    }

    override suspend fun setInt(key: DataSettingAttr, num: Int): Result<Unit> {
        return dataErrorCatching {
            when (key) {
                DataSettingAttr.TUTORIAL -> {
                    dataStore.edit { preferences ->
                        preferences[tutorialKey] = num
                    }
                }
            }
            Unit
        }
    }

    override suspend fun getPublicToken(): Result<String> {
        return dataErrorCatching {
            val token = secureStore.getPublicToken()
            token ?: throw DataError.NotFound("token load fail")
        }
    }

    override suspend fun setPublicToken(token: String): Result<Unit> {
        return dataErrorCatching {
            secureStore.setPublicToken(token)
        }
    }


    override suspend fun setAppVersion(version: String): Result<Unit> {
        return runCatching {
            prefs.edit { putString(prefsLastAppVersion, version) }
        }
    }

    override suspend fun getAppVersion(): Result<String> {
        return runCatching {
            prefs.getString(prefsLastAppVersion, "0.0")!!
        }
    }
}