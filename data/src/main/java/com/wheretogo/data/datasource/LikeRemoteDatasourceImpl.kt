package com.wheretogo.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreTableName
import com.wheretogo.data.LikeObject
import com.wheretogo.data.model.course.RemoteLike
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LikeRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LikeRemoteDatasource {
    private val courseTable = FireStoreTableName.CHECKPOINT_TABLE.name()
    private val likeTable = FireStoreTableName.LIKE_TABLE.name()

    override suspend fun getLikeInObject(type: LikeObject, objectId: String): RemoteLike {

        return suspendCancellableCoroutine { continuation ->
            val objectTable = getObjectTable(type)
            val likeId = getLikeId(objectId)
            firestore.collection(objectTable).document(objectId)
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
            val objectTable = getObjectTable(type)
            val likeId = getLikeId(objectId)
            firestore.collection(objectTable).document(objectId)
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
            val objectTable = getObjectTable(type)
            val likeId = getLikeId(objectId)
            firestore.collection(objectTable).document(objectId)
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

    private fun getObjectTable(likeObject: LikeObject): String {
        return when (likeObject) {
            LikeObject.COURSE_LIKE -> {
                courseTable
            }
        }
    }
}