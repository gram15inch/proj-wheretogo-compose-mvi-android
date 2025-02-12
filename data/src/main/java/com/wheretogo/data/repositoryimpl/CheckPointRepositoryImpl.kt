package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.toCheckPoint
import com.wheretogo.data.toLocalCheckPoint
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.repository.CheckPointRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CheckPointRepositoryImpl @Inject constructor(
    private val checkPointRemoteDatasource: CheckPointRemoteDatasource,
    private val checkPointLocalDatasource: CheckPointLocalDatasource
) : CheckPointRepository {

    override suspend fun setCheckPoint(checkPoint: CheckPoint): Boolean {
        return checkPointRemoteDatasource.setCheckPoint(checkPoint.toRemoteCheckPoint()).apply {
            checkPointLocalDatasource.setCheckPoint(checkPoint.toLocalCheckPoint())
        }
    }

    override suspend fun getCheckPoint(checkPointId: String): CheckPoint? {
        val localCheckPoint = checkPointLocalDatasource.getCheckPoint(checkPointId) ?: run {
            val remoteCheckPoint = checkPointRemoteDatasource.getCheckPoint(checkPointId)
            val remotePath = remoteCheckPoint?.imageName

            remoteCheckPoint?.toLocalCheckPoint(
                localImgUrl = remotePath?:"",
                timestamp = System.currentTimeMillis()
            )?.apply { checkPointLocalDatasource.setCheckPoint(this) }
        }
        return localCheckPoint?.toCheckPoint()
    }


    override suspend fun getCheckPointGroup(
        checkpointIdGroup: List<String>
    ): List<CheckPoint> {
        return checkPointLocalDatasource.getCheckPointGroup(checkpointIdGroup)
            .run {
                coroutineScope {
                    checkpointIdGroup.map { checkPointId ->
                        async {
                            getCheckPoint(checkPointId) // 체크포인트 저장
                        }
                    }.awaitAll()
                }.mapNotNull { it }
            }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Boolean {
        return checkPointRemoteDatasource.removeCheckPoint(checkPointId).apply {
            checkPointLocalDatasource.removeCheckPoint(checkPointId)
        }
    }
}