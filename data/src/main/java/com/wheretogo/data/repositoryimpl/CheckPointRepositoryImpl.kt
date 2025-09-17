package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.di.CheckpointCache
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.toCheckPoint
import com.wheretogo.data.toDomain
import com.wheretogo.data.toLocal
import com.wheretogo.data.toLocalCheckPoint
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.model.util.Snapshot
import com.wheretogo.domain.repository.CheckPointRepository
import de.huxhorn.sulky.ulid.ULID
import javax.inject.Inject

class CheckPointRepositoryImpl @Inject constructor(
    private val checkPointRemoteDatasource: CheckPointRemoteDatasource,
    private val checkPointLocalDatasource: CheckPointLocalDatasource,
    @CheckpointCache private val cachePolicy: CachePolicy
) : CheckPointRepository {

    override suspend fun getCheckPointGroup(
        checkpointIdGroup: List<String>
    ): Result<List<CheckPoint>> {
        return checkPointLocalDatasource
            .getCheckPointGroup(checkpointIdGroup)
            .mapSuccess { localGroup ->
                val expectExpireIdGroup = localGroup
                    .filter { cachePolicy.isExpired(it.timestamp, false) }
                    .map { it.checkPointId }
                if (expectExpireIdGroup.isEmpty())
                    return Result.success(localGroup.toDomain())

                checkPointRemoteDatasource
                    .getCheckPointGroup(expectExpireIdGroup)
                    .mapSuccess { cpGroup ->
                        checkPointLocalDatasource.setCheckPoint(cpGroup.toLocal())
                            .mapCatching { cpGroup.map { it.checkPointId } }
                    }.mapSuccess { actualExpireIdGroup ->
                        expectExpireIdGroup
                            .filter { it !in actualExpireIdGroup }
                            .forEach { checkPointLocalDatasource.removeCheckPoint(it) }
                        checkPointLocalDatasource
                            .getCheckPointGroup(checkpointIdGroup)
                    }
            }.mapCatching { it.toDomain() }.mapDataError().mapDomainError()
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
                checkPointLocalDatasource.removeCheckPoint(checkPointId)
            }.mapDataError().mapDomainError()
    }

    override suspend fun refreshSnapshot(snapshot: Snapshot): Result<Snapshot> {
        return runCatching {
            check(snapshot.refId.isNotBlank()) { "refId Empty!!" }
            if (!cachePolicy.isExpired(
                    timestamp = snapshot.updateAt,
                    isEmpty = snapshot.indexIdGroup.isEmpty()
                )
            ) {
                return Result.success(snapshot)
            }

            Unit
        }.mapSuccess {
            checkPointRemoteDatasource.getCheckPointGroupByCourseId(snapshot.refId)
        }.mapSuccess { remote ->
            checkPointLocalDatasource.setCheckPoint(remote.toLocal())
                .mapCatching {
                    snapshot.copy(
                        indexIdGroup = remote.map { it.checkPointId },
                        updateAt = System.currentTimeMillis()
                    )
                }
        }.mapDataError().mapDomainError()
    }

    override suspend fun forceCacheExpire(checkPointId: String): Result<Unit> {
        return checkPointLocalDatasource.initTimestamp(checkPointId)
            .mapDataError().mapDomainError()
    }

    override suspend fun clearCache(): Result<Unit> {
        return checkPointLocalDatasource.clear().mapDataError().mapDomainError()
    }
}