package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.RemoteCheckPoint

interface CheckPointRemoteDatasource {

    suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): Result<List<RemoteCheckPoint>>

    suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<RemoteCheckPoint>>

    suspend fun setCheckPoint(checkPoint: RemoteCheckPoint): Result<Unit>

    suspend fun updateCheckPoint(checkPointId: String, captioin: String): Result<Unit>

    suspend fun removeCheckPoint(checkPointId: String): Result<Unit>

}