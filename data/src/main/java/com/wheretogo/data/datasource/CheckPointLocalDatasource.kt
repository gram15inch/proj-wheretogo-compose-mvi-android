package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.CheckPointGroup
import com.wheretogo.data.model.checkpoint.LocalCheckPoint

interface CheckPointLocalDatasource {

    suspend fun getCheckPoint(checkPointIdGroup: List<String>): Result<List<LocalCheckPoint>>

    suspend fun getCheckpointGroup(courseId: String): Result<CheckPointGroup?>

    suspend fun setCheckPoint(checkPointGroup: List<LocalCheckPoint>): Result<Unit>

    suspend fun replaceCheckpointGroup(courseId: String, checkPointGroup: List<LocalCheckPoint>): Result<Unit>

    suspend fun clearCheckPointCache(checkPointIdGroup: List<String>): Result<Unit>

    suspend fun clear(): Result<Unit>
}