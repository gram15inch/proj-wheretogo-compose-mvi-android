package com.wheretogo.data.datasource

import com.wheretogo.data.model.checkpoint.CheckPointCluster
import com.wheretogo.data.model.checkpoint.LocalCheckPoint

interface CheckPointLocalDatasource {

    suspend fun getCheckPoints(checkPointIdGroup: List<String>): Result<List<LocalCheckPoint>>

    suspend fun saveCheckPoints(checkPointGroup: List<LocalCheckPoint>): Result<Unit>

    suspend fun deleteCheckPoints(checkPointIdGroup: List<String>): Result<Unit>


    suspend fun getCluster(courseId: String): Result<CheckPointCluster?>

    suspend fun replaceCluster(courseId: String, checkPointGroup: List<LocalCheckPoint>): Result<Unit>


    suspend fun clear(): Result<Unit>
}