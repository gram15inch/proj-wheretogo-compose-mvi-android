package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CHECKPOINT_UPDATE_TIME
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.datasource.ImageLocalDatasource
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
    private val checkPointLocalDatasource: CheckPointLocalDatasource,
    private val imageLocalDatasource: ImageLocalDatasource
) : CheckPointRepository {

    override suspend fun setCheckPoint(checkPoint: CheckPoint): Boolean {
        return checkPointRemoteDatasource.setCheckPoint(checkPoint.toRemoteCheckPoint())
    }

    override suspend fun getCheckPointGroup(metaCheckPoint: MetaCheckPoint): List<CheckPoint> {
        return checkPointLocalDatasource.getCheckPointGroup(metaCheckPoint.toDataMetaCheckPoint())
            .run {
                val isUpdateTime =
                    (System.currentTimeMillis() - metaCheckPoint.timeStamp) >= CHECKPOINT_UPDATE_TIME
                if (isUpdateTime) {// 최소 업데이트 주기 확인
                    metaCheckPoint.loadCheckPoint()
                } else {
                    this@run
                }.toCheckPoint()
            }
    }

    private suspend fun MetaCheckPoint.loadCheckPoint(): List<LocalCheckPoint> {
        return coroutineScope {
            this@loadCheckPoint.checkPointIdGroup.map { checkPointId ->
                async {
                    val remoteCheckPoint = checkPointRemoteDatasource.getCheckPoint(checkPointId)
                    val remotePath = remoteCheckPoint?.imgUrl
                    val localPath =
                        remotePath?.run { imageLocalDatasource.setCheckPointImage(remotePath = remotePath) }  // 가져온 체크포인트 이미지 저장

                    LocalCheckPoint(
                        checkPointId = checkPointId,
                        latLng = remoteCheckPoint?.latLng?.toLatLng() ?: LatLng(),
                        titleComment = remoteCheckPoint?.titleComment ?: "",
                        remoteImgUrl = remotePath ?: "",
                        localImgUrl = localPath ?: "",
                        timestamp = System.currentTimeMillis()
                    ).apply { checkPointLocalDatasource.setCheckPoint(this) } // 체크포인트 저장
                }
            }.awaitAll()
        }
    }
}