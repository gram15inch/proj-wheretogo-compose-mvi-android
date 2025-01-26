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
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
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
    private val like = stringSetPreferencesKey("like_profile")
    private val comment = stringSetPreferencesKey("comment_profile")
    private val course = stringSetPreferencesKey("course_profile")
    private val checkpoint = stringSetPreferencesKey("checkpoint_profile")
    private val report = stringSetPreferencesKey("report_profile")

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


    override suspend fun addHistory(historyId: String, type: HistoryType) {
        val key = getHistoryKey(type)
        userDataStore.edit { preferences ->
            preferences[key] =
                preferences[key]?.toMutableSet()?.apply { this += historyId } ?: setOf(historyId)
        }
    }

    override suspend fun removeHistory(historyId: String, type: HistoryType) {
        val key = getHistoryKey(type)
        userDataStore.edit { preferences ->
            preferences[key] =
                preferences[key]?.toMutableSet()?.apply { this -= historyId } ?: emptySet()
        }
    }

    private fun getHistoryKey(type: HistoryType): Preferences.Key<Set<String>> {
        return when (type) {
            HistoryType.LIKE -> like
            HistoryType.BOOKMARK -> bookmark
            HistoryType.COMMENT -> comment
            HistoryType.COURSE -> course
            HistoryType.CHECKPOINT -> checkpoint
            HistoryType.REPORT -> report
        }
    }

    override fun getHistoryFlow(type: HistoryType): Flow<HashSet<String>> {
        val key = getHistoryKey(type)
        return userDataStore.data.map { preferences ->
            preferences[key]?.toHashSet() ?: hashSetOf()
        }
    }

    override suspend fun setProfile(profile: Profile) {
        userDataStore.edit { preferences ->
            preferences[uid] = profile.uid
            preferences[name] = profile.public.name
            preferences[mail] = profile.private.mail
            preferences[authCompany] = profile.private.authCompany
            preferences[lastVisitedDate] = profile.private.lastVisited
            preferences[accountCreationDate] = profile.private.accountCreation
            preferences[isAdRemove] = profile.private.isAdRemove
        }
    }

    override suspend fun setHistory(history: History) {
        userDataStore.edit { preferences ->
            preferences[bookmark] = history.bookmarkGroup
            preferences[like] = history.likeGroup
            preferences[comment] = history.commentGroup
            preferences[checkpoint] = history.checkpointGroup
            preferences[course] = history.courseGroup
            preferences[report] = history.reportGroup
        }
    }

    override suspend fun clearUser() {
        setProfile(Profile())
        setHistory(History())
    }

    override fun getProfileFlow(): Flow<Profile> {
        return userDataStore.data.map { preferences ->
            preferences[uid].let { uid ->
                check(uid != null) { "user not exist: $uid" }
                check(uid.isNotBlank()) { "user not exist: $uid" }
            }
            Profile(
                uid = (preferences[uid] ?: ""),
                public = ProfilePublic(
                    name = preferences[name] ?: ""
                ),
                private = ProfilePrivate(
                    mail = (preferences[mail] ?: ""),
                    authCompany = preferences[authCompany] ?: "",
                    lastVisited = preferences[lastVisitedDate] ?: 0L,
                    accountCreation = preferences[accountCreationDate] ?: 0L,
                    isAdRemove = preferences[isAdRemove] ?: false
                ),
            )
        }
    }
}