package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.CheckpointPolicy
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.di.CheckpointCache
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.toCheckPoint
import com.wheretogo.data.toDomain
import com.wheretogo.data.toDomainResult
import com.wheretogo.data.toLocal
import com.wheretogo.data.toLocalCheckPoint
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.feature.sucessMap
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.repository.CheckPointRepository
import de.huxhorn.sulky.ulid.ULID
import timber.log.Timber
import javax.inject.Inject

class CheckPointRepositoryImpl @Inject constructor(
    private val checkPointRemoteDatasource: CheckPointRemoteDatasource,
    private val checkPointLocalDatasource: CheckPointLocalDatasource,
    @CheckpointCache private val cachePolicy: CachePolicy
) : CheckPointRepository {

    private suspend fun refreshAndGetCheckPointGroup(
        checkPointGroup: List<LocalCheckPoint>
    ): Result<List<LocalCheckPoint>> {
        val expireGroup = checkPointGroup
            .filter { it.timestamp == 0L }
            .map { it.checkPointId }
        if (expireGroup.isEmpty())
            return Result.success(checkPointGroup)

        // 만료된 체크포인트 리프레시
        return checkPointRemoteDatasource
            .getCheckPointGroup(expireGroup)
            .onSuccess { remoteGroup ->
                // 갱신된 체크포인트 로컬 저장
                checkPointLocalDatasource.setCheckPoint(remoteGroup.toLocal())
                    .mapCatching { remoteGroup.map { it.checkPointId } }
                val actualExpireIdGroup = remoteGroup.map { it.checkPointId }
                // 갱신 했으나 서버에 없는 체크포인트 삭제
                expireGroup
                    .filter { it !in actualExpireIdGroup }
                    .forEach { checkPointLocalDatasource.removeCheckPoint(it) }
            }.map { it.toLocal() }
    }

    override suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<CheckPoint>> {
        val old = checkPointLocalDatasource.getLatestUpdate().getOrDefault(0L)
        val now = System.currentTimeMillis()
        val num =
            (now - old).toFloat() / (1000 * 60 * if (old == 0L) CheckpointPolicy.minuteWhenEmpty else CheckpointPolicy.minuteWhenNotEmpty)
        val formatStr = String.format("%.1f%%", num * 100)

        // 전체 만료 체크
        return if (cachePolicy.isExpired(old, old == 0L).apply {
                Timber.d("cp expire: $formatStr $this")
            }) {
            checkPointLocalDatasource.setLatestUpdate(now)
            checkPointRemoteDatasource.getCheckPointGroupByCourseId(courseId)
                .map { it.toLocal() }
                .onSuccess { checkPointLocalDatasource.replaceCheckpointByCourse(courseId, it) }
        } else {
            //개별 만료 체크
            checkPointLocalDatasource.getCheckpointByCourseId(courseId).sucessMap {
                refreshAndGetCheckPointGroup(it)
            }
        }.map { it.toDomain() }.toDomainResult()
    }

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
            checkPointLocalDatasource.getCheckPoint(checkPointId)
                .map { it ?: throw DataError.NotFound("$checkPointId NOT_FOUND") }
        }.map { it.toCheckPoint() }
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

    override suspend fun forceCacheExpire(checkPointId: String): Result<Unit> {
        return checkPointLocalDatasource.initTimestamp(checkPointId)
            .mapDataError().mapDomainError()
    }

    override suspend fun clearCache(): Result<Unit> {
        return checkPointLocalDatasource.clear().mapDataError().mapDomainError()
    }
}