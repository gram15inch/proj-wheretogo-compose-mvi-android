package com.wheretogo.data.datasourceimpl

import androidx.room.Transaction
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.checkpoint.CheckPointCluster
import com.wheretogo.data.model.checkpoint.CheckPointGroupMeta
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import javax.inject.Inject

class CheckPointLocalDatasourceImpl @Inject constructor(
    private val checkPointDatabase: CheckPointDatabase,
) : CheckPointLocalDatasource {
    private val checkPointDao by lazy { checkPointDatabase.checkPointDao() }


    override suspend fun getCheckPoints(checkPointIdGroup: List<String>): Result<List<LocalCheckPoint>> {
        return dataErrorCatching { checkPointDao.select(checkPointIdGroup) }
    }

    override suspend fun saveCheckPoints(checkPointGroup: List<LocalCheckPoint>): Result<Unit> {
        return dataErrorCatching { checkPointDao.upsert(checkPointGroup) }
    }

    override suspend fun deleteCheckPoints(checkPointIdGroup: List<String>): Result<Unit> {
        return runCatching { checkPointDao.delete(checkPointIdGroup) }
    }


    override suspend fun getCluster(courseId: String): Result<CheckPointCluster?> {
        return runCatching {
            checkPointDao.selectGroup(courseId)
        }
    }

    @Transaction
    override suspend fun replaceCluster(
        courseId: String,
        checkPointGroup: List<LocalCheckPoint>
    ): Result<Unit> {
        return dataErrorCatching {
            checkPointDao.deleteGroup(courseId)
            checkPointDao.upsert(checkPointGroup)
            checkPointDao.upsertMeta(CheckPointGroupMeta(courseId, System.currentTimeMillis()))
        }
    }


    override suspend fun clear(): Result<Unit> {
        return dataErrorCatching {
            checkPointDatabase.clearAllTables()
        }
    }
}