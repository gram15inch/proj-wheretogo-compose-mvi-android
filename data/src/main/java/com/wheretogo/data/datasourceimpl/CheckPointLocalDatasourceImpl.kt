package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CheckPointLocalDatasourceImpl @Inject constructor(
    private val checkPointDatabase: CheckPointDatabase
) : CheckPointLocalDatasource {
    private val checkPointDao by lazy { checkPointDatabase.checkPointDao() }
    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): List<LocalCheckPoint> {
        return coroutineScope {
            checkPointIdGroup.map {
                async {
                    checkPointDao.select(it)
                }
            }.awaitAll().mapNotNull { it }
        }
    }

    override suspend fun getCheckPoint(checkPointId: String): LocalCheckPoint? {
        return checkPointDao.select(checkPointId)
    }

    override suspend fun setCheckPoint(checkPoint: LocalCheckPoint) {
        checkPointDao.insert(checkPoint)
    }

    override suspend fun removeCheckPoint(checkPointId: String) {
        checkPointDao.delete(checkPointId)
    }

    override suspend fun updateCheckPoint(checkPointId: String, caption: String) {
        checkPointDao.update(checkPointId, caption)
    }

}