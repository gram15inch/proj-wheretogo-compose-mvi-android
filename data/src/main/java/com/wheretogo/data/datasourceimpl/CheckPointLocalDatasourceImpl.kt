package com.wheretogo.data.datasourceimpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class CheckPointLocalDatasourceImpl @Inject constructor(
    private val checkPointDatabase: CheckPointDatabase,
    @Named("contentDataStore") private val contentDataStore: DataStore<Preferences>
) : CheckPointLocalDatasource {
    private val checkPointDao by lazy { checkPointDatabase.checkPointDao() }
    private val checkPointCacheKey = longPreferencesKey("checkPointCache")

    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): Result<List<LocalCheckPoint>> {
        return runCatching { checkPointDao.selectByGroup(checkPointIdGroup) }
    }

    override suspend fun getCheckPoint(checkPointId: String): Result<LocalCheckPoint?> {
        return runCatching { checkPointDao.select(checkPointId) }
    }

    override suspend fun getCheckpointByCourseId(courseId: String): Result<List<LocalCheckPoint>> {
        return dataErrorCatching { checkPointDatabase.checkPointDao().selectByCourseId(courseId) }
    }

    override suspend fun setCheckPoint(checkPointGroup: List<LocalCheckPoint>): Result<Unit> {
        return runCatching { checkPointDao.insert(checkPointGroup) }
    }

    override suspend fun replaceCheckpointByCourse(
        courseId: String,
        checkPointGroup: List<LocalCheckPoint>
    ): Result<Unit> {
        return runCatching {
            val newIdGroup = checkPointGroup.map { it.checkPointId }
            val staleIdGroup = checkPointDao.selectByCourseId(courseId)
                .filter { it.checkPointId !in newIdGroup }
                .map { it.checkPointId }
            checkPointDao.deleteByGroup(staleIdGroup)
            checkPointDao.insert(checkPointGroup)
        }
    }

    override suspend fun getLatestUpdate(): Result<Long> {
        return dataErrorCatching {
            contentDataStore.data.map { preferences ->
                preferences[checkPointCacheKey] ?: 0
            }.first()
        }
    }

    override suspend fun setLatestUpdate(updateAt: Long): Result<Unit> {
        return dataErrorCatching {
            contentDataStore.edit { preferences ->
                preferences[checkPointCacheKey] = updateAt
            }
            Unit
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Result<Unit> {
        return runCatching { checkPointDao.delete(checkPointId) }
    }

    override suspend fun updateCheckPoint(checkPointId: String, caption: String): Result<Unit> {
        return runCatching { checkPointDao.updateCaption(checkPointId, caption) }
    }

    override suspend fun initTimestamp(checkPointId: String): Result<Unit> {
        return runCatching { checkPointDao.updateTimestamp(checkPointId, 0) }
    }

    override suspend fun clear(): Result<Unit> {
        return dataErrorCatching {
            setLatestUpdate(0L)
            checkPointDatabase.clearAllTables()
        }
    }
}