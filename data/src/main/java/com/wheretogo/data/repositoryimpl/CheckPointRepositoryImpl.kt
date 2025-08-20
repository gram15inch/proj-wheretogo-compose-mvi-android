package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.di.CheckpointCache
import com.wheretogo.data.toCheckPoint
import com.wheretogo.data.toLocalCheckPoint
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Snapshot
import com.wheretogo.domain.repository.CheckPointRepository
import okio.FileNotFoundException
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

    override suspend fun getCheckPoint(checkPointId: String): Result<CheckPoint> {
        return runCatching {
            val localCheckPoint = checkPointLocalDatasource.getCheckPoint(checkPointId) ?: run {
                 val localGroup= checkPointRemoteDatasource.getCheckPoint(checkPointId)?.toLocalCheckPoint()
                     ?: return  Result.failure(FileNotFoundException("$checkPointId not found"))
                checkPointLocalDatasource.setCheckPoint(listOf(localGroup))
                localGroup
            }
            localCheckPoint.toCheckPoint()
        }
    }

    override suspend fun getCheckPointBySnapshot(snapshot: Snapshot): Result<List<CheckPoint>> {
        return runCatching {
            if (cachePolicy.isExpired(
                    timestamp = snapshot.timeStamp,
                    isEmpty = snapshot.indexIdGroup.isEmpty()
                )
            ) {
                getCheckpointByCourse(snapshot.refId)
            } else {
                checkPointLocalDatasource.getCheckPointGroup(snapshot.indexIdGroup)
                    .map { it.toCheckPoint() }
            }
        }
    }

    private suspend fun getCheckpointByCourse(courseId:String):List<CheckPoint>{
        val group= checkPointRemoteDatasource.getCheckPointByCourseId(courseId)
        checkPointLocalDatasource.replaceCheckpointByCourse(courseId, group.map { it.toLocalCheckPoint() })
        return group.map { it.toCheckPoint() }
    }

    override suspend fun getCheckPointGroup(
        checkpointIdGroup: List<String>
    ): Result<List<CheckPoint>> {
        return runCatching {
            val localGroup= checkPointLocalDatasource.getCheckPointGroup(checkpointIdGroup)
            val updateGroup= checkpointIdGroup - localGroup.map { it.checkPointId }.toSet()
            if(updateGroup.isNotEmpty()) {
                localGroup + checkPointRemoteDatasource.getCheckPointGroup(updateGroup).map { it.toLocalCheckPoint() }
            } else {
                localGroup
            }.map { it.toCheckPoint() }
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Result<Unit> {
        return runCatching {
            checkPointRemoteDatasource.removeCheckPoint(checkPointId)
            checkPointLocalDatasource.removeCheckPoint(checkPointId)
        }
    }

    override suspend fun updateCaption(checkPointId: String, caption: String): Result<Unit> {
        return runCatching {
            checkPointRemoteDatasource.updateCheckPoint(checkPointId, caption)
            checkPointLocalDatasource.updateCheckPoint(checkPointId, caption)
        }
    }
}