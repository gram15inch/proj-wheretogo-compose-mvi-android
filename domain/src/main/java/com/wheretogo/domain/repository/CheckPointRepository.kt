package com.wheretogo.domain.repository

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest

interface CheckPointRepository {

    suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<CheckPoint>>

    suspend fun getCheckPoint(checkPointId: String, isRemote: Boolean): Result<CheckPoint>

    suspend fun addCheckPoint(request: CheckPointAddRequest): Result<CheckPoint>

    suspend fun removeCheckPoint(checkPointId: String): Result<Unit>

    suspend fun refreshCheckPoint(checkpointIds: List<String>): Result<Unit>

    suspend fun clearCache(): Result<Unit>
}