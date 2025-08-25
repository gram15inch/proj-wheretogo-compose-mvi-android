package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.LocalCheckPoint

interface CheckPointLocalDatasource {

    suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): List<LocalCheckPoint>

    suspend fun getCheckPoint(checkPointId: String): LocalCheckPoint?

    suspend fun setCheckPoint(checkPointGroup: List<LocalCheckPoint>)

    suspend fun replaceCheckpointByCourse(courseId: String, checkPointGroup: List<LocalCheckPoint>)

    suspend fun removeCheckPoint(checkPointId: String)

    suspend fun updateCheckPoint(checkPointId: String, caption: String)

    suspend fun initTimestamp(checkPointId: String)

}