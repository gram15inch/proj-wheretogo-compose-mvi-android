package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.RemoteCheckPoint

interface CheckPointRemoteDatasource {

    suspend fun getCheckPointGroup(checkPoints: List<String>): List<RemoteCheckPoint>

    suspend fun getCheckPoint(checkPointId: String): RemoteCheckPoint?

    suspend fun setCheckPoint(checkPoint: RemoteCheckPoint)

    suspend fun removeCheckPoint(checkPointId: String)

}