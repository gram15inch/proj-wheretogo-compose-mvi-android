package com.wheretogo.data.datasourceimpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.history.LocalHistoryGroupWrapper
import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.data.model.user.LocalProfilePrivate
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.History
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.let

class UserLocalDatasourceImpl @Inject constructor(
    private val userDataStore: DataStore<Preferences>
) : UserLocalDatasource {
    private val uid = stringPreferencesKey("uid_profile")
    private val mail = stringPreferencesKey("mail_profile")
    private val hashMail = stringPreferencesKey("hashmail_profile")
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
    private val reportContent = stringSetPreferencesKey("report_content_profile")

    private val commentAddedAt = longPreferencesKey("comment_added_at_profile")
    private val courseAddedAt = longPreferencesKey("course_added_at_profile")
    private val checkpointAddedAt = longPreferencesKey("checkpoint_added_at_profile")


    override suspend fun addHistory(type: HistoryType, historyId: String, addedAt: Long): Result<Unit> {
        return dataErrorCatching {
            val historyKey = getHistoryKey(type)
            val addedAtKey = getHistoryAddedAtKey(type)
            userDataStore.edit { preferences ->
                historyKey.let { key ->
                    preferences[key] =
                        preferences[key]?.toMutableSet()?.apply { this += historyId }
                            ?: setOf(historyId)
                }

                addedAtKey?.let { key ->
                    preferences[key] = addedAt
                }
            }

            Unit
        }
    }

    override suspend fun removeHistory(type: HistoryType, historyId: String): Result<Unit> {
        return dataErrorCatching {
            val historyKey = getHistoryKey(type)
            userDataStore.edit { preferences ->
                preferences[historyKey] = preferences[historyKey]?.toMutableSet()?.apply { this -= historyId } ?: emptySet()
            }
            Unit
        }
    }

    private fun getHistoryKey(type: HistoryType): Preferences.Key<Set<String>> {
        return when (type) {
            HistoryType.LIKE -> like
            HistoryType.BOOKMARK -> bookmark
            HistoryType.COMMENT -> comment
            HistoryType.COURSE -> course
            HistoryType.CHECKPOINT -> checkpoint
            HistoryType.REPORT -> reportContent
        }
    }

    private fun getHistoryAddedAtKey(type: HistoryType): Preferences.Key<Long>?{
        return when (type) {
            HistoryType.COMMENT -> commentAddedAt
            HistoryType.COURSE -> courseAddedAt
            HistoryType.CHECKPOINT -> checkpointAddedAt
            else -> null
        }
    }

    override suspend fun getHistory(type: HistoryType): LocalHistoryGroupWrapper {
        val historyKey = getHistoryKey(type)
        val addedAtKey = getHistoryAddedAtKey(type)
        val historyId = userDataStore.data.map { preferences ->
            preferences[historyKey]?.toHashSet() ?: hashSetOf()
        }.first()

        val addedAt = if(addedAtKey==null) 0L else  userDataStore.data.map { preferences ->
            preferences[addedAtKey]?:0L
        }.first()

        return LocalHistoryGroupWrapper(
            type = type,
            historyIdGroup = historyId,
            lastAddedAt = addedAt
        )
    }

    override suspend fun setProfile(profile: LocalProfile): Result<Unit> {
        return dataErrorCatching {
            userDataStore.edit { preferences ->
                preferences[uid] = profile.uid
                preferences[name] = profile.name
                preferences[hashMail] = profile.hashMail
                preferences[mail] = profile.private.mail
                preferences[authCompany] = profile.private.authCompany
                preferences[lastVisitedDate] = profile.private.lastVisited
                preferences[accountCreationDate] = profile.private.accountCreation
                preferences[isAdRemove] = profile.private.isAdRemove
            }
            Unit
        }
    }

    override suspend fun setHistory(history: History): Result<Unit> {
        return dataErrorCatching {
            userDataStore.edit { preferences ->
                preferences[course] = history.course.historyIdGroup
                preferences[checkpoint] = history.checkpoint.historyIdGroup
                preferences[comment] = history.comment.historyIdGroup
                preferences[like] = history.like.historyIdGroup
                preferences[reportContent] = history.report.historyIdGroup
                preferences[bookmark] = history.bookmark.historyIdGroup

                preferences[courseAddedAt] = history.course.lastAddedAt
                preferences[commentAddedAt] = history.comment.lastAddedAt
                preferences[checkpointAddedAt] = history.checkpoint.lastAddedAt
            }
            Unit
        }
    }

    override suspend fun clearUser(): Result<Unit> {
        return dataErrorCatching {
            setProfile(LocalProfile())
            setHistory(History())
            Unit
        }
    }

    override fun getProfileFlow(): Flow<LocalProfile> {
        return userDataStore.data.map { preferences ->
            LocalProfile(
                uid = (preferences[uid] ?: ""),
                name = (preferences[name] ?: ""),
                hashMail = (preferences[hashMail] ?: ""),
                private = LocalProfilePrivate(
                    mail = (preferences[mail] ?: ""),
                    authCompany = preferences[authCompany] ?: "",
                    lastVisited = preferences[lastVisitedDate] ?: 0L,
                    accountCreation = preferences[accountCreationDate] ?: 0L,
                    isAdRemove = preferences[isAdRemove] ?: false
                )
            )
        }
    }
}