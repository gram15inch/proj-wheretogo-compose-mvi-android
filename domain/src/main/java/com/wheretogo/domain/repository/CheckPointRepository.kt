package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.MetaCheckPoint

interface CheckPointRepository {

    suspend fun setCheckPoint(checkPoint: CheckPoint): Boolean

    suspend fun getCheckPointGroup(metaCheckPoint: MetaCheckPoint): List<CheckPoint>

}