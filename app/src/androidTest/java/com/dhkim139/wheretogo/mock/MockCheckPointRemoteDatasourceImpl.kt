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

    override suspend fun getCheckPointByCourseId(courseId: String): List<RemoteCheckPoint> {
        return newCheckPointGroup.filter { it.courseId == courseId }
    }

    override suspend fun setCheckPoint(checkPoint: RemoteCheckPoint) {
        newCheckPointGroup.add(checkPoint)
    }

    override suspend fun removeCheckPoint(checkPointId: String) {
        newCheckPointGroup.removeIf { it.courseId == checkPointId }
    }

    override suspend fun updateCheckPoint(checkPointId: String, captioin: String) {
        getCheckPoint(checkPointId)?.let{
            removeCheckPoint(checkPointId)
            newCheckPointGroup.add(
               it.copy(caption = captioin)
            )
        }
    }
}