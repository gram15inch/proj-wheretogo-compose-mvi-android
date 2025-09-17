package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.LocalCheckPoint

interface CheckPointLocalDatasource {

    suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): Result<List<LocalCheckPoint>>

    suspend fun getCheckPoint(checkPointId: String): Result<LocalCheckPoint?>

    suspend fun setCheckPoint(checkPointGroup: List<LocalCheckPoint>): Result<Unit>

    suspend fun replaceCheckpointByCourse(courseId: String, checkPointGroup: List<LocalCheckPoint>): Result<Unit>

    suspend fun removeCheckPoint(checkPointId: String): Result<Unit>

    suspend fun updateCheckPoint(checkPointId: String, caption: String): Result<Unit>

    suspend fun initTimestamp(checkPointId: String): Result<Unit>

    suspend fun clear(): Result<Unit>
}