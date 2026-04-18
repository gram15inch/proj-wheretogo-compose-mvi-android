package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.di.CheckpointCache
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.checkpoint.isExpired
import com.wheretogo.data.toCheckPoint
import com.wheretogo.data.toDomain
import com.wheretogo.data.toDomainResult
import com.wheretogo.data.toLocal
import com.wheretogo.data.toLocalCheckPoint
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.feature.successMap
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.repository.CheckPointRepository
import de.huxhorn.sulky.ulid.ULID
import javax.inject.Inject

class CheckPointRepositoryImpl @Inject constructor(
    private val checkPointRemoteDatasource: CheckPointRemoteDatasource,
    private val checkPointLocalDatasource: CheckPointLocalDatasource,
    @CheckpointCache private val cachePolicy: CachePolicy
) : CheckPointRepository {

    override suspend fun getCheckPoint(
        checkPointId: String,
        isRemote: Boolean
    ): Result<CheckPoint> {
        return if (isRemote) {
            checkPointRemoteDatasource.getCheckPointGroup(listOf(checkPointId)).mapCatching {
                it.firstOrNull() ?: throw DataError.NotFound("$checkPointId NOT_FOUND")
            }.map { it.toLocalCheckPoint() }.onSuccess {
                checkPointLocalDatasource.setCheckPoint(listOf(it))
            }
        } else {
            checkPointLocalDatasource.getCheckPoint(listOf(checkPointId))
                .map { it.firstOrNull() ?: throw DataError.NotFound("$checkPointId NOT_FOUND") }
        }.map { it.toCheckPoint() }
    }

    override suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<CheckPoint>> {
        return checkPointLocalDatasource.getCheckpointGroup(courseId).successMap { group ->
            if(group == null || group.isExpired(cachePolicy)){
                checkPointRemoteDatasource.getCheckPointGroupByCourseId(courseId)
                    .map { it.toLocal() }
                    .onSuccess { checkPointLocalDatasource.replaceCheckpointGroup(courseId, it) }
            }else{
                Result.success(group.items)
            }.map { it.toDomain() }.toDomainResult()
        }
    }

    override suspend fun addCheckPoint(request: CheckPointAddRequest): Result<CheckPoint> {
        val checkPointId = "CP${ULID().nextULID()}"
        val remote = request.toRemoteCheckPoint(checkPointId)
        val local = remote.toLocalCheckPoint()
        return checkPointRemoteDatasource.setCheckPoint(remote)
            .mapSuccess {
                checkPointLocalDatasource.setCheckPoint(listOf(local))
            }.mapCatching {
                val imageLocalPath = request.image.uriPathGroup[ImageSize.SMALL]!!
                local.toCheckPoint(imageLocalPath)
            }.mapDataError().mapDomainError()
    }

    override suspend fun removeCheckPoint(checkPointId: String): Result<Unit> {
        return checkPointRemoteDatasource.removeCheckPoint(checkPointId)
            .mapSuccess {
                checkPointLocalDatasource.clearCheckPointCache(listOf(checkPointId))
            }.mapDataError().mapDomainError()
    }

    override suspend fun refreshCheckPoint(checkpointIds: List<String>): Result<Unit> {
        return checkPointRemoteDatasource.getCheckPointGroup(checkpointIds).mapSuccess {
            checkPointLocalDatasource.setCheckPoint(it.toLocal())
        }.mapDataError().mapDomainError()
    }

    override suspend fun clearCache(): Result<Unit> {
        return checkPointLocalDatasource.clear().mapDataError().mapDomainError()
    }
}