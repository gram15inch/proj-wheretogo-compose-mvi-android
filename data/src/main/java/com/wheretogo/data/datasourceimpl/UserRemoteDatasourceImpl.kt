package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wheretogo.data.DataBuildConfig
import com.wheretogo.data.DataHistoryType
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.UserApiService
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.DataMsgToken
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.data.toDataError
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor(
    private val userApiService: UserApiService,
    buildConfig: DataBuildConfig
) : UserRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val userRootCollection = buildConfig.dbPrefix +  FireStoreCollections.USER.name
    override suspend fun deleteUser(userId: String): Result<String> {
       return dataErrorCatching {
            userApiService.deleteUser(userId = userId)
        }.mapSuccess {
           if (!it.isSuccessful)
                Result.failure(it.toDataError())
           else
               Result.success(userId)
        }
    }

    override suspend fun updateMsgToken(token: String): Result<Unit> {
        return dataErrorCatching {
            userApiService.updateMsgToken(DataMsgToken(token))
        }.mapSuccess {
            if (!it.isSuccessful)
                Result.failure(it.toDataError())
            else
                Result.success(Unit)
        }
    }


    override suspend fun getProfilePublic(uid: String): Result<RemoteProfilePublic> {
        return dataErrorCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(userRootCollection).document(uid)
                    .get()
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
            snapshot.toObject(RemoteProfilePublic::class.java)
        }
    }

    override suspend fun getProfilePublicWithMail(hashMail: String): Result<RemoteProfilePublic> {
        return dataErrorCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(userRootCollection)
                    .whereEqualTo(RemoteProfilePublic::hashMail.name, hashMail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            continuation.resume(result)
                        } else {
                            continuation.resume(null)
                        }
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
            snapshot?.first()?.toObject(RemoteProfilePublic::class.java)
        }
    }

    override suspend fun getProfilePrivate(userId: String): Result<RemoteProfilePrivate> {
        return dataErrorCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(userRootCollection).document(userId)
                    .collection(FireStoreCollections.PRIVATE.name)
                    .document(FireStoreCollections.PRIVATE.name)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result != null && result.exists()) {
                            continuation.resume(result)

                        } else {
                            continuation.resume(null)
                        }
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
            snapshot?.toObject(RemoteProfilePrivate::class.java)
        }
    }

    override suspend fun deleteProfile(uid: String): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                val root = firestore.collection(userRootCollection).document(uid)
                root.collection(FireStoreCollections.HISTORY.name)
                    .document(FireStoreCollections.BOOKMARK.name).delete()
                root.collection(FireStoreCollections.HISTORY.name)
                    .document(FireStoreCollections.LIKE.name).delete()
                root.collection(FireStoreCollections.HISTORY.name)
                    .document(FireStoreCollections.COMMENT.name).delete()
                root.collection(FireStoreCollections.HISTORY.name)
                    .document(FireStoreCollections.COURSE.name).delete()
                root.collection(FireStoreCollections.HISTORY.name)
                    .document(FireStoreCollections.BOOKMARK.name).delete()
                root.collection(FireStoreCollections.PRIVATE.name).document(uid).delete()
                root.delete()
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        }
    }

    override suspend fun getHistoryGroup(uid: String): Result<List<RemoteHistoryGroupWrapper>> {
        return dataErrorCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(userRootCollection).document(uid)
                    .collection(FireStoreCollections.HISTORY.name)
                    .get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
            snapshot.toObjects(RemoteHistoryGroupWrapper::class.java)
        }
    }

    override suspend fun setHistoryGroup(
        uid: String,
        wrapper: RemoteHistoryGroupWrapper
    ): Result<Long> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(userRootCollection).document(uid)
                    .collection(FireStoreCollections.HISTORY.name)
                    .document(wrapper.type.toCollectionName())
                    .set(wrapper)
                    .addOnSuccessListener { _ ->
                        continuation.resume(wrapper.lastAddedAt)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        }
    }

    override suspend fun addHistory(
        type: DataHistoryType,
        uid: String,
        groupId: String,
        historyId: String
    ): Result<Long> {
        val addedAt= System.currentTimeMillis()
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                val field = "${RemoteHistoryGroupWrapper::historyIdGroup.name}.$groupId"
                firestore.collection(userRootCollection).document(uid)
                    .collection(FireStoreCollections.HISTORY.name)
                    .document(type.toCollectionName())
                    .update(
                        mapOf(
                            field to FieldValue.arrayUnion(historyId),
                            RemoteHistoryGroupWrapper::lastAddedAt.name to addedAt
                        )
                    )
                    .addOnSuccessListener {
                        continuation.resume(false)
                    }.addOnFailureListener { e ->
                        if (e is FirebaseFirestoreException) {
                            return@addOnFailureListener when (e.code) {
                                FirebaseFirestoreException.Code.NOT_FOUND -> {
                                    continuation.resume(true)
                                }

                                else -> {
                                    continuation.resumeWithException(e)
                                }
                            }
                        }
                        continuation.resumeWithException(e)
                    }
                }
        }.mapSuccess { isNotFound ->
            if (isNotFound)
                setHistoryGroup(
                    uid = uid,
                    wrapper = RemoteHistoryGroupWrapper(
                        type = type,
                        historyIdGroup = mapOf(groupId to listOf(historyId)),
                        lastAddedAt = addedAt
                    )
                )
            else
                Result.success(addedAt)
        }
    }

    override suspend fun removeHistory(
        type: DataHistoryType,
        uid: String,
        groupId: String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                val field = "${RemoteHistoryGroupWrapper::historyIdGroup.name}.$groupId"
                firestore.collection(userRootCollection).document(uid)
                    .collection(FireStoreCollections.HISTORY.name)
                    .document(type.toCollectionName())
                    .update(
                        mapOf(
                            field to FieldValue.arrayRemove(historyId)
                        )
                    ).addOnSuccessListener { _ ->
                        continuation.resume(Unit)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        }
    }

    private fun DataHistoryType.toCollectionName(): String {
        return when (this) {
            DataHistoryType.COURSE -> FireStoreCollections.COURSE.name
            DataHistoryType.CHECKPOINT -> FireStoreCollections.CHECKPOINT.name
            DataHistoryType.COMMENT -> FireStoreCollections.COMMENT.name
            DataHistoryType.LIKE -> FireStoreCollections.LIKE.name
            DataHistoryType.REPORT -> FireStoreCollections.REPORT.name
        }
    }
}