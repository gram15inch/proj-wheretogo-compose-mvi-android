package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.collections.map
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CheckPointRemoteDatasourceImpl @Inject constructor() : CheckPointRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val checkPointRootCollection = FireStoreCollections.CHECKPOINT.name()
    private val checkPointIdAttr = RemoteCheckPoint::checkPointId.name

    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): Result<List<RemoteCheckPoint>> {
        return runCatching {
            val snapshot= suspendCancellableCoroutine { continuation ->
                firestore.collection(checkPointRootCollection)
                    .whereIn(checkPointIdAttr, checkPointIdGroup).get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.map { it.toObject(RemoteCheckPoint::class.java) }
        }
    }

    override suspend fun getCheckPointGroupByCourseId(courseId: String): Result<List<RemoteCheckPoint>> {
        return runCatching {
            val snapshot=  suspendCancellableCoroutine { continuation ->
                firestore.collection(checkPointRootCollection)
                    .whereEqualTo(RemoteCheckPoint::courseId.name, courseId)
                    .get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.toObjects(RemoteCheckPoint::class.java)
        }
    }

    override suspend fun setCheckPoint(checkPoint: RemoteCheckPoint): Result<Unit> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(checkPointRootCollection).document(checkPoint.checkPointId)
                    .set(checkPoint).addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }
    }

    override suspend fun updateCheckPoint(checkPointId: String, captioin: String): Result<Unit> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(checkPointRootCollection).document(checkPointId)
                    .update(RemoteCheckPoint::caption.name, captioin).addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Result<Unit> {
        return  runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(checkPointRootCollection).document(checkPointId)
                    .delete()
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }
    }
}