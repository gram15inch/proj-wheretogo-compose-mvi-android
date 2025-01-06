package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.CheckPoint

interface CheckPointRepository {

    suspend fun setCheckPoint(checkPoint: CheckPoint): Boolean

    suspend fun getCheckPoint(checkPointId: String): CheckPoint

    suspend fun getCheckPointGroup(
        checkpointIdGroup: List<String>
    ): List<CheckPoint>

}