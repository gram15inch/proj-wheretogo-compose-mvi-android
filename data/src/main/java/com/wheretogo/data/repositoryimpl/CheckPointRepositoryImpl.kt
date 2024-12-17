package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.toCheckPoint
import com.wheretogo.data.toDataMetaCheckPoint
import com.wheretogo.data.toLatLng
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
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
        return checkPointRemoteDatasource.setCheckPoint(checkPoint.toRemoteCheckPoint())
    }

    override suspend fun getCheckPoint(checkPointId: String): CheckPoint {
        val localCheckPoint = checkPointLocalDatasource.getCheckPoint(checkPointId) ?: run {
            val remoteCheckPoint = checkPointRemoteDatasource.getCheckPoint(checkPointId)
            val remotePath = remoteCheckPoint?.imgUrl

            LocalCheckPoint(
                checkPointId = checkPointId,
                latLng = remoteCheckPoint?.latLng?.toLatLng() ?: LatLng(),
                titleComment = remoteCheckPoint?.titleComment ?: "",
                remoteImgUrl = remotePath ?: "",
                localImgUrl = "",
                timestamp = System.currentTimeMillis()
            ).apply { checkPointLocalDatasource.setCheckPoint(this) }
        }
        return localCheckPoint.toCheckPoint()
    }


    override suspend fun getCheckPointGroup(
        metaCheckPoint: MetaCheckPoint,
        isFetch: Boolean
    ): List<CheckPoint> {
        return checkPointLocalDatasource.getCheckPointGroup(metaCheckPoint.toDataMetaCheckPoint())
            .run {
                if (isFetch) {
                    coroutineScope {
                        metaCheckPoint.checkPointIdGroup.map { checkPointId ->
                            async {
                                getCheckPoint(checkPointId) // 체크포인트 저장
                            }
                        }.awaitAll()
                    }
                } else {
                    this@run.toCheckPoint()
                }
            }
    }

}