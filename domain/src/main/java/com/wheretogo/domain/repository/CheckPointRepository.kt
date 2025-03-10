package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.CheckPoint

interface CheckPointRepository {

    suspend fun setCheckPoint(checkPoint: CheckPoint): Result<Unit>

    suspend fun getCheckPoint(checkPointId: String): Result<CheckPoint>

    suspend fun getCheckPointGroup(
        checkpointIdGroup: List<String>
    ): Result<List<CheckPoint>>

    suspend fun removeCheckPoint(checkPointId: String): Result<Unit>

}