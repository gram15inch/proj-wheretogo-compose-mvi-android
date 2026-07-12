package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.di.CheckpointCache
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.model.checkpoint.isExpired
import com.wheretogo.data.toCheckPoint
import com.wheretogo.data.toCreateContent
import com.wheretogo.data.toDomain
import com.wheretogo.data.toLocal
import com.wheretogo.data.toLocalCheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.repository.CheckPointRepository
import timber.log.Timber
import javax.inject.Inject

class CheckPointRepositoryImpl @Inject constructor(
    private val checkPointRemoteDatasource: CheckPointRemoteDatasource,
    private val checkPointLocalDatasource: CheckPointLocalDatasource,
    @CheckpointCache private val cachePolicy: CachePolicy
) : CheckPointRepository {

    override suspend fun getCheckPoint(
        checkPointId: String,
        isRemote: Boolean
    ): Result<CheckPoint?> {
       return runCatching {
            val local= checkPointLocalDatasource.getCheckPoints(listOf(checkPointId))
                .map { it.firstOrNull() }.getOrNull()
           if (local == null && isRemote) {
               fetchCheckPoint(listOf(checkPointId)).firstOrNull()
           } else local
        }.map { it?.toCheckPoint() }
    }

    override suspend fun getCheckPoints(
        checkPointIds: List<String>,
    ): Result<List<CheckPoint>> {
        return runCatching {
            val local= checkPointLocalDatasource.getCheckPoints(checkPointIds).getOrThrow()
            val missingIds = local.map { it.checkPointId }.filter { !checkPointIds.contains(it) }
            if (missingIds.isNotEmpty()) {
                fetchCheckPoint(missingIds)
            } else local
        }.map { it.toDomain() }
    }

    override suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<CheckPoint>> {
        val localGroup = checkPointLocalDatasource.getCluster(courseId).getOrNull()
        return if (localGroup == null || localGroup.isExpired(cachePolicy)) {
            checkPointRemoteDatasource.getCheckPointGroupByCourseId(courseId)
                .map { it.toLocal() }
                .onSuccess { localList ->
                    checkPointLocalDatasource.replaceCluster(courseId, localList)
                }.map { it.toDomain() }
        } else {
            Result.success(localGroup.items.toDomain())
        }.mapDomainError()
    }

    override suspend fun addCheckPoint(request: CheckPointAddRequest): Result<CheckPoint> {
        return checkPointRemoteDatasource.setCheckPoint(request.toCreateContent())
            .mapSuccess { remote ->
                val local = remote.toLocalCheckPoint()
                checkPointLocalDatasource.saveCheckPoints(listOf(local)).map { local }
            }.mapCatching { local ->
                local.toCheckPoint()
            }.mapDataError().mapDomainError()
    }

    override suspend fun removeCheckPoint(checkPointId: String, courseId: String): Result<Unit> {
        return checkPointRemoteDatasource.removeCheckPoint(checkPointId, courseId)
            .mapSuccess {
                checkPointLocalDatasource.deleteCheckPoints(listOf(checkPointId))
            }.mapDataError().mapDomainError()
    }

    override suspend fun refreshCheckPoint(checkpointIds: List<String>): Result<Unit> {
        return runCatching {
            fetchCheckPoint(checkpointIds)
        }.map { }.mapDataError().mapDomainError()
    }

    override suspend fun clearCache(): Result<Unit> {
        return checkPointLocalDatasource.clear().mapDataError().mapDomainError()
    }

    private suspend fun fetchCheckPoint(checkpointIds: List<String>): List<LocalCheckPoint> {
        val remote =
            checkPointRemoteDatasource.getCheckPointGroup(checkpointIds)
                .onFailure {
                    Timber.i("checkpoint_ $checkpointIds NOT_FOUND")
                }.getOrDefault(emptyList())

        val local = remote.toLocal()
        checkPointLocalDatasource.saveCheckPoints(local).getOrThrow()
        return local
    }
}