package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.di.CheckpointCache
import com.wheretogo.data.toDomain
import com.wheretogo.data.toLocal
import com.wheretogo.data.toLocalCheckPoint
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Snapshot
import com.wheretogo.domain.repository.CheckPointRepository
import javax.inject.Inject

class CheckPointRepositoryImpl @Inject constructor(
    private val checkPointRemoteDatasource: CheckPointRemoteDatasource,
    private val checkPointLocalDatasource: CheckPointLocalDatasource,
    @CheckpointCache private val cachePolicy: CachePolicy
) : CheckPointRepository {

    override suspend fun setCheckPoint(checkPoint: CheckPoint): Result<Unit> {
        return runCatching {
            checkPointRemoteDatasource.setCheckPoint(checkPoint.toRemoteCheckPoint())
            checkPointLocalDatasource.setCheckPoint(listOf(checkPoint.toLocalCheckPoint()))
        }
    }

    override suspend fun getCheckPointGroup(
        checkpointIdGroup: List<String>
    ): Result<List<CheckPoint>> {
        return runCatching {
            val localGroup= checkPointLocalDatasource
                .getCheckPointGroup(checkpointIdGroup)

            val expectExpireIdGroup= localGroup
                .filter { cachePolicy.isExpired(it.timestamp,false) }
                .map { it.checkPointId }

            if(expectExpireIdGroup.isEmpty())
                return@runCatching localGroup.toDomain()

            val actualExpireIdGroup = checkPointRemoteDatasource
                .getCheckPointGroup(expectExpireIdGroup).map { it.checkPointId }

            expectExpireIdGroup
                .filter { it !in actualExpireIdGroup }
                .forEach { checkPointLocalDatasource.removeCheckPoint(it) }

            checkPointLocalDatasource
                .getCheckPointGroup(checkpointIdGroup).toDomain()
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Result<Unit> {
        return runCatching {
            checkPointRemoteDatasource.removeCheckPoint(checkPointId)
            checkPointLocalDatasource.removeCheckPoint(checkPointId)
        }
    }

    override suspend fun refreshSnapshot(snapshot: Snapshot): Result<Snapshot> {
        return runCatching {
            check(snapshot.refId.isNotBlank()) { "refId Empty!!" }

            if (!cachePolicy.isExpired(
                    timestamp = snapshot.timeStamp,
                    isEmpty = snapshot.indexIdGroup.isEmpty()
                )
            ) {
                return@runCatching snapshot
            }
            val remote = checkPointRemoteDatasource.getCheckPointGroupByCourseId(snapshot.refId).apply {
                checkPointLocalDatasource.setCheckPoint(this.toLocal())
            }.map { it.checkPointId }
            snapshot.copy(remote, timeStamp = System.currentTimeMillis())
        }
    }

    override suspend fun updateCaption(checkPointId: String, caption: String): Result<Unit> {
        return runCatching {
            checkPointRemoteDatasource.updateCheckPoint(checkPointId, caption)
            checkPointLocalDatasource.updateCheckPoint(checkPointId, caption)
        }
    }

    override suspend fun forceCacheExpire(checkPointId: String): Result<Unit> {
        return runCatching {
            checkPointLocalDatasource.initTimestamp(checkPointId)
        }
    }
}