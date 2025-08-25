package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.RemoteCheckPoint

interface CheckPointRemoteDatasource {

    suspend fun getCheckPoint(checkPointId: String): RemoteCheckPoint?

    suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): List<RemoteCheckPoint>

    suspend fun getCheckPointGroupByCourseId(courseId: String): List<RemoteCheckPoint>

    suspend fun setCheckPoint(checkPoint: RemoteCheckPoint)

    suspend fun updateCheckPoint(checkPointId: String, captioin: String)

    suspend fun removeCheckPoint(checkPointId: String)

}