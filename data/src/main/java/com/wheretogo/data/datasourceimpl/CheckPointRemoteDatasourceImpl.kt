package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.DataBuildConfig
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.ContentApiService
import com.wheretogo.data.feature.safeApiCall
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointCreateContent
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CheckPointRemoteDatasourceImpl @Inject constructor(
    buildConfig: DataBuildConfig,
    private val contentApiService: ContentApiService
) : CheckPointRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val checkPointRootCollection =
        buildConfig.dbPrefix + FireStoreCollections.CHECKPOINT.name

    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): Result<List<RemoteCheckPoint>> {
        return runCatching {
            checkPointIdGroup.chunked(30).flatMap { chunk ->
                firestore.collection(checkPointRootCollection)
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(RemoteCheckPoint::class.java)?.copy(id = doc.id)
                    }
            }
        }
    }

    override suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<RemoteCheckPoint>> {
        return runCatching {
            firestore.collection(checkPointRootCollection)
                .whereEqualTo(RemoteCheckPoint::courseId.name, courseId)
                .get()
                .await()
                .documents
                .mapNotNull { doc -> doc.toObject(RemoteCheckPoint::class.java)?.copy(id = doc.id) }
        }
    }

    override suspend fun setCheckPoint(checkPoint: RemoteCheckPoint): Result<Unit> {
        return runCatching {
            firestore.collection(checkPointRootCollection).document(checkPoint.id)
                .set(checkPoint)
                .await()
        }
    }

    override suspend fun setCheckPoint(content: CheckPointCreateContent): Result<RemoteCheckPoint> {
        return safeApiCall {
            contentApiService.addCheckPoint(content)
        }
    }

    override suspend fun updateCheckPoint(checkPointId: String, caption: String): Result<Unit> {
        return runCatching {
            firestore.collection(checkPointRootCollection).document(checkPointId)
                .update(RemoteCheckPoint::caption.name, caption)
                .await()
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Result<Unit> {
        return runCatching {
            firestore.collection(checkPointRootCollection).document(checkPointId)
                .delete()
                .await()
        }
    }
}