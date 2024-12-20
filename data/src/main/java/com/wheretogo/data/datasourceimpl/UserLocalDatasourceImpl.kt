package com.wheretogo.data.datasourceimpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.domain.HistoryType
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

    private val bookmark = stringSetPreferencesKey("bookmark_profile2") // 2 유지
    private val like = stringSetPreferencesKey("like_profile")


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


    override suspend fun addHistory(code: String, type: HistoryType) {
        val key = getHistoryKey(type)
        userDataStore.edit { preferences ->
            preferences[key] =
                preferences[key]?.toMutableSet()?.apply { this += code } ?: setOf(code)
        }
    }

    override suspend fun removeHistory(code: String, type: HistoryType) {
        val key = getHistoryKey(type)
        userDataStore.edit { preferences ->
            preferences[key] =
                preferences[key]?.toMutableSet()?.apply { this -= code } ?: emptySet()
        }
    }

    private fun getHistoryKey(type: HistoryType): Preferences.Key<Set<String>> {
        return when (type) {
            HistoryType.LIKE -> like
            HistoryType.BOOKMARK -> bookmark
        }
    }

    override fun getHistoryFlow(type: HistoryType): Flow<List<String>> {
        val key = getHistoryKey(type)
        return userDataStore.data.map { preferences ->
            (preferences[key]?.toSet()?.toList() ?: emptyList<String>())
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

            preferences[bookmark] = profile.bookMarkGroup.toSet()
            preferences[like] = profile.likeGroup.toSet()
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

    override suspend fun clearHistory() {
        val likeKey = getHistoryKey(HistoryType.LIKE)
        val bookmarkKey = getHistoryKey(HistoryType.BOOKMARK)

        userDataStore.edit { preferences ->
            preferences[likeKey] = emptySet()
            preferences[bookmarkKey] = emptySet()
        }
    }
}