package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CheckPointRemoteDatasourceImpl @Inject constructor() : CheckPointRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val checkPointRootCollection = FireStoreCollections.CHECKPOINT.name()
    private val checkPointIdAttr = RemoteCheckPoint::checkPointId.name

    override suspend fun getCheckPointGroup(checkPoints: List<String>): List<RemoteCheckPoint> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointRootCollection)
                .whereIn(checkPointIdAttr, checkPoints.chunked(10)).get()
                .addOnSuccessListener {
                    val group = it.map { it.toObject(RemoteCheckPoint::class.java) }
                    continuation.resume(group)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getCheckPoint(checkPointId: String): RemoteCheckPoint? {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointRootCollection).document(checkPointId).get()
                .addOnSuccessListener {
                    continuation.resume(it.toObject(RemoteCheckPoint::class.java))
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getCheckPointByCourseId(courseId: String): List<RemoteCheckPoint> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointRootCollection)
                .whereEqualTo(RemoteCheckPoint::courseId.name, courseId)
                .get()
                .addOnSuccessListener {
                    val group = it.toObjects(RemoteCheckPoint::class.java)
                    continuation.resume(group)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setCheckPoint(checkPoint: RemoteCheckPoint) {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointRootCollection).document(checkPoint.checkPointId)
                .set(checkPoint).addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun updateCheckPoint(checkPointId: String, captioin: String) {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointRootCollection).document(checkPointId)
                .update(RemoteCheckPoint::caption.name, captioin).addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String) {
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