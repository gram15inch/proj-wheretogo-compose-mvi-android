package com.wheretogo.domain.repository

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.model.util.Snapshot

interface CheckPointRepository {

    suspend fun getCheckPointGroup(checkpointIdGroup: List<String>): Result<List<CheckPoint>>

    suspend fun addCheckPoint(request: CheckPointAddRequest): Result<CheckPoint>

    suspend fun removeCheckPoint(checkPointId: String): Result<Unit>

    suspend fun refreshSnapshot(snapshot: Snapshot): Result<Snapshot>

    suspend fun forceCacheExpire(checkPointId: String): Result<Unit>

    suspend fun clearCache(): Result<Unit>
}