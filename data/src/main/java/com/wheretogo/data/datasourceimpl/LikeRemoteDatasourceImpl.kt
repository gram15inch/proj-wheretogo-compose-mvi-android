package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.LikeObject
import com.wheretogo.data.datasource.LikeRemoteDatasource
import com.wheretogo.data.model.course.RemoteLike
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LikeRemoteDatasourceImpl @Inject constructor() : LikeRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val courseCollection = FireStoreCollections.CHECKPOINT.name()
    private val likeTable = FireStoreCollections.LIKE.name()

    override suspend fun getLikeInObject(type: LikeObject, objectId: String): RemoteLike {

        return suspendCancellableCoroutine { continuation ->
            val objectCollection = getObjectCollection(type)
            val likeId = getLikeId(objectId)
            firestore.collection(objectCollection).document(objectId)
                .collection(likeTable).document(likeId)
                .get()
                .addOnSuccessListener {
                    val route = it.toObject(RemoteLike::class.java) ?: RemoteLike()
                    continuation.resume(route)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setLikeInObject(
        type: LikeObject,
        objectId: String,
        remoteLike: RemoteLike
    ): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val objectCollection = getObjectCollection(type)
            val likeId = getLikeId(objectId)
            firestore.collection(objectCollection).document(objectId)
                .collection(likeTable).document(likeId)
                .set(remoteLike)
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }


    override suspend fun removeLikeInCourse(type: LikeObject, objectId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val objectCollection = getObjectCollection(type)
            val likeId = getLikeId(objectId)
            firestore.collection(objectCollection).document(objectId)
                .collection(likeTable).document(likeId)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }

    override fun getLikeId(objectId: String): String {
        return "${objectId}_like"
    }

    private fun getObjectCollection(likeObject: LikeObject): String {
        return when (likeObject) {
            LikeObject.COURSE_LIKE -> {
                courseCollection
            }
        }
    }
}