package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.LocalCheckPoint

interface CheckPointLocalDatasource {

    suspend fun getCheckPointGroup(checkPoints: List<String>): List<LocalCheckPoint>

    suspend fun getCheckPoint(checkPointId: String): LocalCheckPoint?

    suspend fun setCheckPoint(checkPoint: LocalCheckPoint): Boolean

}