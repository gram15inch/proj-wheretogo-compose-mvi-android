package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CheckPointDao
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CheckPointLocalDatasourceImpl @Inject constructor(
    private val checkPointDao: CheckPointDao
) : CheckPointLocalDatasource {

    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): List<LocalCheckPoint> {
        return coroutineScope {
            checkPointIdGroup.map {
                async {
                    checkPointDao.select(it)
                }
            }.mapNotNull { it.await() }
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

}