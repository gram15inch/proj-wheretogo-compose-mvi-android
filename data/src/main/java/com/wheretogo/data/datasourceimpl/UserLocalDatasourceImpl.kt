package com.wheretogo.data.datasourceimpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserLocalDatasourceImpl @Inject constructor(
    private val userDataStore: DataStore<Preferences>
) : UserLocalDatasource {
    private val isRequestLoginKey = booleanPreferencesKey("isRequestLoginKey")

    private val uid = stringPreferencesKey("uid_profile")
    private val mail = stringPreferencesKey("mail_profile")
    private val name = stringPreferencesKey("name_profile")
    private val authCompany = stringPreferencesKey("authCompany_profile")
    private val lastVisitedDate = longPreferencesKey("lastVisitedDate_profile")
    private val accountCreationDate = longPreferencesKey("accountCreationDate_profile")
    private val isAdRemove = booleanPreferencesKey("isAdRemove_profile")

    private val bookmark = stringSetPreferencesKey("bookmark_profile")


    override fun isRequestLoginFlow(): Flow<Boolean> {
        return userDataStore.data.map { preferences ->
            preferences[isRequestLoginKey] ?: true
        }
    }

    override suspend fun setRequestLogin(boolean: Boolean) {
        userDataStore.edit { preferences ->
            preferences[isRequestLoginKey] = boolean
        }
    }


    override suspend fun addBookmark(code: String) {
        userDataStore.edit { preferences ->
            preferences[bookmark] = (preferences[bookmark]?.toMutableSet()
                ?: mutableSetOf()).apply { this += code }
        }
    }

    override suspend fun removeBookmark(code: String) {
        userDataStore.edit { preferences ->
            preferences[bookmark] = (preferences[bookmark]?.toMutableSet()
                ?: mutableSetOf()).apply { this -= code }
        }
    }

    override fun getBookmarkFlow(): Flow<List<String>> {
        return userDataStore.data.map { preferences ->
            preferences[bookmark]?.map { it } ?: emptyList()
        }
    }

    override suspend fun setProfile(profile: Profile) {
        userDataStore.edit { preferences ->
            preferences[uid] = profile.uid
            preferences[mail] = profile.mail
            preferences[name] = profile.name
            preferences[authCompany] = profile.authCompany
            preferences[lastVisitedDate] = profile.lastVisited
            preferences[accountCreationDate] = profile.accountCreation
            preferences[isAdRemove] = profile.isAdRemove
        }
    }

    override suspend fun clearUser() {
        setProfile(Profile())
    }

    override fun getProfileFlow(): Flow<Profile> {
        return userDataStore.data.map { preferences ->
            Profile(
                uid = (preferences[uid] ?: ""),
                mail = (preferences[mail] ?: ""),
                name = preferences[name] ?: "",
                authCompany = preferences[authCompany] ?: "",
                lastVisited = preferences[lastVisitedDate] ?: 0L,
                accountCreation = preferences[accountCreationDate] ?: 0L,
                isAdRemove = preferences[isAdRemove] ?: false
            )
        }
    }
}