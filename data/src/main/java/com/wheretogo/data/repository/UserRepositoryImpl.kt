package com.wheretogo.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val userDataStore: DataStore<Preferences>) :
    UserRepository {

    private val isRequestLoginKey = booleanPreferencesKey("isRequestLoginKey")
    private val id = stringPreferencesKey("id_profile")
    private val name = stringPreferencesKey("name_profile")
    private val lastVisitedDate = longPreferencesKey("lastVisitedDate_profile")
    private val accountCreationDate = longPreferencesKey("accountCreationDate_profile")
    private val isAdRemove = booleanPreferencesKey("isAdRemove_profile")
    private val bookmark = stringSetPreferencesKey("bookmark_profile")


    override suspend fun isRequestLoginFlow(): Flow<Boolean> {
        return userDataStore.data.map { preferences ->
            preferences[isRequestLoginKey] ?: true
        }
    }

    override suspend fun setRequestLogin(boolean: Boolean) {
        userDataStore.edit { preferences ->
            preferences[isRequestLoginKey] = boolean
        }
    }


    override suspend fun addBookmark(code: Int) {
        userDataStore.edit { preferences ->
            preferences[bookmark] = (preferences[bookmark]?.toMutableSet()
                ?: mutableSetOf()).apply { this += code.toString() }
        }
    }

    override suspend fun removeBookmark(code: Int) {
        userDataStore.edit { preferences ->
            preferences[bookmark] = (preferences[bookmark]?.toMutableSet()
                ?: mutableSetOf()).apply { this -= code.toString() }
        }
    }


    override suspend fun getBookmarkFlow(): Flow<List<Int>> {
        return userDataStore.data.map { preferences ->
            preferences[bookmark]?.map { it.toInt() } ?: emptyList()
        }
    }

    override suspend fun setProfile(profile: Profile) {
        userDataStore.edit { preferences ->
            preferences[id] = profile.id
            preferences[name] = profile.name
            preferences[lastVisitedDate] = profile.lastVisitedDate
            preferences[accountCreationDate] = profile.accountCreationDate
            preferences[isAdRemove] = profile.isAdRemove
        }
    }

    override fun getProfileFlow(): Flow<Profile> {
        return userDataStore.data.map { preferences ->
            Profile(
                id = (preferences[id] ?: ""),
                name = preferences[name] ?: "",
                lastVisitedDate = preferences[lastVisitedDate] ?: 0L,
                accountCreationDate = preferences[accountCreationDate] ?: 0L,
                isAdRemove = preferences[isAdRemove] ?: false
            )
        }
    }
}