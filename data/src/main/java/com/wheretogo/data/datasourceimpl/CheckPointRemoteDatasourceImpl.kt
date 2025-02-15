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
    private val checkPointTable = FireStoreCollections.CHECKPOINT.name()

    override suspend fun getCheckPointGroup(checkPoints: List<String>): List<RemoteCheckPoint> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointTable)
                .whereIn(RemoteCheckPoint::checkPointId.name, checkPoints.chunked(10)).get()
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
            firestore.collection(checkPointTable).document(checkPointId).get()
                .addOnSuccessListener {
                    continuation.resume(it.toObject(RemoteCheckPoint::class.java))
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setCheckPoint(checkPoint: RemoteCheckPoint): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointTable).document(checkPoint.checkPointId)
                .set(checkPoint).addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun removeCheckPoint(checkPointId: String): Boolean {
        return suspendCancellableCoroutine<Boolean> { continuation ->
            firestore.collection(checkPointTable).document(checkPointId)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }
}