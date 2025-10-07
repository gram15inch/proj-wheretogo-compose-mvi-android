package com.wheretogo.data.datasourceimpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.history.LocalHistory
import com.wheretogo.data.model.history.LocalHistoryGroupWrapper
import com.wheretogo.data.model.history.LocalHistoryIdGroup
import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.data.model.user.LocalProfilePrivate
import com.wheretogo.domain.HistoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import javax.inject.Inject

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
    private val isAdmin = booleanPreferencesKey("isAdmin_profile")

    private val like = byteArrayPreferencesKey("like_profile")
    private val comment = byteArrayPreferencesKey("comment_profile")
    private val course = byteArrayPreferencesKey("course_profile")
    private val checkpoint = byteArrayPreferencesKey("checkpoint_profile")
    private val reportContent = byteArrayPreferencesKey("report_content_profile")

    private val commentAddedAt = longPreferencesKey("comment_added_at_profile")
    private val courseAddedAt = longPreferencesKey("course_added_at_profile")
    private val checkpointAddedAt = longPreferencesKey("checkpoint_added_at_profile")

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun addHistory(
        type: HistoryType,
        groupId: String,
        historyId: String,
        addedAt: Long
    ): Result<Unit> {
        return dataErrorCatching {
            val historyKey = getHistoryKey(type)
            val addedAtKey = getHistoryAddedAtKey(type)
            userDataStore.edit { preferences ->
                if (historyId.isNotBlank())
                    historyKey.let { key ->
                        val old = preferences[key]?.run {
                            Cbor.decodeFromByteArray(this)
                        } ?: LocalHistoryIdGroup()
                        val new = old.run {
                            copy(
                                groupById = groupById.toMutableMap().apply {
                                    this[groupId] = this[groupId]?.toMutableSet()?.apply {
                                        add(historyId)
                                    }?.toHashSet() ?: hashSetOf(historyId)
                                }
                            )
                        }
                        preferences[key] = Cbor.encodeToByteArray(new)
                    }

                addedAtKey?.let { key ->
                    preferences[key] = addedAt
                }
            }

            Unit
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun removeHistory(
        type: HistoryType,
        groupId: String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            val historyKey = getHistoryKey(type)
            userDataStore.edit { preferences ->
                if (historyId.isNotBlank())
                    historyKey.let { key ->
                        val old = preferences[key]?.run {
                            Cbor.decodeFromByteArray(this)
                        } ?: LocalHistoryIdGroup()
                        val new = old.run {
                            copy(
                                groupById = groupById.toMutableMap().apply {
                                    this[groupId] = this[groupId]?.toMutableSet()?.apply {
                                        remove(historyId)
                                    }?.toHashSet() ?: hashSetOf(historyId)
                                }
                            )
                        }
                        preferences[key] = Cbor.encodeToByteArray(new)
                    }
            }
            Unit
        }
    }

    private fun getHistoryKey(type: HistoryType): Preferences.Key<ByteArray> {
        return when (type) {
            HistoryType.LIKE -> like
            HistoryType.COMMENT -> comment
            HistoryType.COURSE -> course
            HistoryType.CHECKPOINT -> checkpoint
            HistoryType.REPORT -> reportContent
        }
    }

    private fun getHistoryAddedAtKey(type: HistoryType): Preferences.Key<Long>? {
        return when (type) {
            HistoryType.COMMENT -> commentAddedAt
            HistoryType.COURSE -> courseAddedAt
            HistoryType.CHECKPOINT -> checkpointAddedAt
            else -> null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getHistory(type: HistoryType): LocalHistoryGroupWrapper {
        val historyKey = getHistoryKey(type)
        val addedAtKey = getHistoryAddedAtKey(type)
        val historyIdGroup = userDataStore.data.map { preferences ->
            preferences[historyKey]?.run { Cbor.decodeFromByteArray(this) } ?: LocalHistoryIdGroup()
        }.first()

        val addedAt = if (addedAtKey == null) 0L else userDataStore.data.map { preferences ->
            preferences[addedAtKey] ?: 0L
        }.first()

        return LocalHistoryGroupWrapper(
            type = type,
            historyIdGroup = historyIdGroup,
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
                preferences[isAdmin] = profile.private.isAdmin
            }
            Unit
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun iniHistory(history: LocalHistory): Result<Unit> {
        return dataErrorCatching {
            userDataStore.edit { preferences ->
                preferences[course] = Cbor.encodeToByteArray(history.course.historyIdGroup)
                preferences[checkpoint] = Cbor.encodeToByteArray(history.checkpoint.historyIdGroup)
                preferences[comment] = Cbor.encodeToByteArray(history.comment.historyIdGroup)
                preferences[like] = Cbor.encodeToByteArray(history.like.historyIdGroup)
                preferences[reportContent] = Cbor.encodeToByteArray(history.report.historyIdGroup)

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
            iniHistory(LocalHistory())
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
                    isAdRemove = preferences[isAdRemove] ?: false,
                    isAdmin = preferences[isAdmin] ?: false
                )
            )
        }
    }
}