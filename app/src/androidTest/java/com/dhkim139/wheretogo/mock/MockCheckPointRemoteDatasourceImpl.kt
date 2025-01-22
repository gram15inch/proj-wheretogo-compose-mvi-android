package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import javax.inject.Inject

class MockCheckPointRemoteDatasourceImpl @Inject constructor() : CheckPointRemoteDatasource {
    private var newCheckPointGroup = mutableListOf<RemoteCheckPoint>()
    override suspend fun getCheckPointGroup(checkPoints: List<String>): List<RemoteCheckPoint> {
        return newCheckPointGroup.mapNotNull {
            if (it.checkPointId in checkPoints)
                it
            else
                null
        }
    }

    override suspend fun getCheckPoint(checkPointId: String): RemoteCheckPoint? {
        return newCheckPointGroup.firstOrNull {
            it.checkPointId in checkPointId
        }
    }

    override suspend fun setCheckPoint(checkPoint: RemoteCheckPoint): Boolean {
        return newCheckPointGroup.add(checkPoint)
    }

    override suspend fun removeCheckPoint(checkPointId: String): Boolean {
        return newCheckPointGroup.removeIf { it.checkPointId == checkPointId }
    }
}