package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.model.course.DataMetaCheckPoint

interface CheckPointLocalDatasource {

    suspend fun getCheckPointGroup(dataMetaCheckPoint: DataMetaCheckPoint): List<LocalCheckPoint>

    suspend fun getCheckPoint(checkPointId: String): LocalCheckPoint?

    suspend fun setCheckPoint(checkPoint: LocalCheckPoint)

    suspend fun removeCheckPoint(checkPointId: String)

}