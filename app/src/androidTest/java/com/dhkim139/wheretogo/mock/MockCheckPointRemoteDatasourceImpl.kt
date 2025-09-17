package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import javax.inject.Inject

class MockCheckPointRemoteDatasourceImpl @Inject constructor() : CheckPointRemoteDatasource {
    private var newCheckPointGroup = mutableListOf<RemoteCheckPoint>()
    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): Result<List<RemoteCheckPoint>> {
        return runCatching {
            newCheckPointGroup.mapNotNull {
                if (it.checkPointId in checkPointIdGroup)
                    it
                else
                    null
            }
        }
    }

    override suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<RemoteCheckPoint>> {
        return runCatching { newCheckPointGroup.filter { it.courseId == courseId } }
    }

    override suspend fun setCheckPoint(checkPoint: RemoteCheckPoint): Result<Unit> {
        return runCatching {
            newCheckPointGroup.add(checkPoint)
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Result<Unit> {
        return runCatching {
            newCheckPointGroup.removeIf { it.courseId == checkPointId }
        }
    }

    override suspend fun updateCheckPoint(checkPointId: String, captioin: String): Result<Unit> {
        return getCheckPointGroup(listOf(checkPointId))
            .mapSuccess { cpGroup ->
                removeCheckPoint(checkPointId)
                    .mapSuccess {
                        runCatching {
                            newCheckPointGroup.add(
                                cpGroup.first().copy(caption = captioin)
                            )
                        }
                    }.mapCatching { Unit }
            }
    }
}