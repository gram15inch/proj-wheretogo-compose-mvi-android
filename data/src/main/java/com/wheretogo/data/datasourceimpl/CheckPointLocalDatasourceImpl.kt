package com.wheretogo.data.datasourceimpl

import androidx.room.Transaction
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.checkpoint.CheckPointGroup
import com.wheretogo.data.model.checkpoint.CheckPointGroupMeta
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import javax.inject.Inject

class CheckPointLocalDatasourceImpl @Inject constructor(
    private val checkPointDatabase: CheckPointDatabase,
) : CheckPointLocalDatasource {
    private val checkPointDao by lazy { checkPointDatabase.checkPointDao() }

    override suspend fun getCheckPoint(checkPointIdGroup: List<String>): Result<List<LocalCheckPoint>> {
        return dataErrorCatching { checkPointDao.select(checkPointIdGroup) }
    }

    override suspend fun getCheckpointGroup(courseId: String): Result<CheckPointGroup?> {
        return runCatching {
            checkPointDao.getCheckPointGroup(courseId)
        }
    }

    override suspend fun setCheckPoint(checkPointGroup: List<LocalCheckPoint>): Result<Unit> {
        return dataErrorCatching { checkPointDao.insert(checkPointGroup) }
    }

    @Transaction
    override suspend fun replaceCheckpointGroup(
        courseId: String,
        checkPointGroup: List<LocalCheckPoint>
    ): Result<Unit> {
        return dataErrorCatching {
            checkPointDao.upsertMeta(CheckPointGroupMeta(courseId, System.currentTimeMillis()))
            checkPointDao.upsert(checkPointGroup)
        }
    }

    override suspend fun clearCheckPointCache(checkPointIdGroup: List<String>): Result<Unit> {
        return runCatching { checkPointDao.delete(checkPointIdGroup) }
    }

    override suspend fun clear(): Result<Unit> {
        return dataErrorCatching {
            checkPointDatabase.clearAllTables()
        }
    }
}