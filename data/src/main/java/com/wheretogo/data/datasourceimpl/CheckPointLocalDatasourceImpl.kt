package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import javax.inject.Inject

class CheckPointLocalDatasourceImpl @Inject constructor(
) : CheckPointLocalDatasource {

    override suspend fun getCheckPointGroup(checkPoints: List<String>): List<LocalCheckPoint> {
        return emptyList()
    }

    override suspend fun getCheckPoint(checkPointId: String): LocalCheckPoint? {
        return null
    }

    override suspend fun setCheckPoint(checkPoint: LocalCheckPoint): Boolean {
        return false
    }

}