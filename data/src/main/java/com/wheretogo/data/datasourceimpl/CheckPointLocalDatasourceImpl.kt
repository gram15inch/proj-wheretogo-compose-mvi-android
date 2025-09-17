package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import javax.inject.Inject

class CheckPointLocalDatasourceImpl @Inject constructor(
    private val checkPointDatabase: CheckPointDatabase
) : CheckPointLocalDatasource {
    private val checkPointDao by lazy { checkPointDatabase.checkPointDao() }
    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): Result<List<LocalCheckPoint>> {
        return runCatching { checkPointDao.selectByGroup(checkPointIdGroup) }
    }

    override suspend fun getCheckPoint(checkPointId: String): Result<LocalCheckPoint?> {
        return runCatching { checkPointDao.select(checkPointId) }
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
        return dataErrorCatching { checkPointDatabase.clearAllTables() }
    }
}