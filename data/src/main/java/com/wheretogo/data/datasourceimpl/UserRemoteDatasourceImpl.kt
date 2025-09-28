package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.FirebaseApiService
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.data.name
import com.wheretogo.data.toDataError
import com.wheretogo.domain.HistoryType
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor(
    private val firebaseApiService: FirebaseApiService
) : UserRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override suspend fun deleteUser(userId: String): Result<String> {
       return dataErrorCatching {
            firebaseApiService.deleteUser(userId = userId)
        }.mapSuccess {
           if (!it.isSuccessful)
                Result.failure(it.toDataError())
           else
               Result.success(userId)
        }
    }

    override suspend fun setProfilePublic(public: RemoteProfilePublic): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(FireStoreCollections.USER.name())
                    .document(public.uid)
                    .set(public)
                    .addOnSuccessListener { _ ->
                        continuation.resume(Unit)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        }
    }

    override suspend fun setProfilePrivate(
        uid: String,
        privateProfile: RemoteProfilePrivate
    ): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(FireStoreCollections.USER.name()).document(uid)
                    .collection(FireStoreCollections.PRIVATE.name)
                    .document(FireStoreCollections.PRIVATE.name)
                    .set(privateProfile)
                    .addOnSuccessListener { _ ->
                        continuation.resume(Unit)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        }
    }

    override suspend fun getProfilePublic(uid: String): Result<RemoteProfilePublic> {
        return dataErrorCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(FireStoreCollections.USER.name()).document(uid)
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
                firestore.collection(FireStoreCollections.USER.name())
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
                firestore.collection(FireStoreCollections.USER.name()).document(userId)
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
                val root = firestore.collection(FireStoreCollections.USER.name()).document(uid)
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


    override suspend fun setHistoryGroup(
        uid: String,
        wrapper: RemoteHistoryGroupWrapper
    ): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(FireStoreCollections.USER.name()).document(uid)
                    .collection(FireStoreCollections.HISTORY.name)
                    .document(wrapper.type.toCollectionName())
                    .set(wrapper)
                    .addOnSuccessListener { _ ->
                        continuation.resume(Unit)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        }
    }

    override suspend fun removeHistory(uid: String, historyId: String, type: HistoryType): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(FireStoreCollections.USER.name()).document(uid)
                    .collection(FireStoreCollections.HISTORY.name)
                    .document(type.toCollectionName())
                    .update(
                        RemoteHistoryGroupWrapper::historyIdGroup.name,
                        FieldValue.arrayRemove(historyId)
                    ).addOnSuccessListener { _ ->
                        continuation.resume(Unit)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }
        }
    }

    override suspend fun addHistory(
        uid: String,
        historyId: String,
        type: HistoryType
    ): Result<Long> {
        val addedAt= System.currentTimeMillis()
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(FireStoreCollections.USER.name()).document(uid)
                    .collection(FireStoreCollections.HISTORY.name)
                    .document(type.toCollectionName())
                    .update(
                        mapOf(
                            RemoteHistoryGroupWrapper::historyIdGroup.name to FieldValue.arrayUnion(historyId),
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
        }.mapSuccess { isNull ->
            val historyGroup = if(historyId.isBlank()) emptyList() else listOf(historyId)
            if (isNull)
                setHistoryGroup(uid, RemoteHistoryGroupWrapper(type, historyGroup, addedAt))
            Result.success(addedAt)
        }
    }

    override suspend fun getHistoryGroup(uid: String): Result<List<RemoteHistoryGroupWrapper>> {
        return dataErrorCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(FireStoreCollections.USER.name()).document(uid)
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

    private fun HistoryType.toCollectionName(): String {
        return when (this) {
            HistoryType.COURSE -> FireStoreCollections.COURSE.name
            HistoryType.CHECKPOINT -> FireStoreCollections.CHECKPOINT.name
            HistoryType.COMMENT -> FireStoreCollections.COMMENT.name
            HistoryType.LIKE -> FireStoreCollections.LIKE.name
            HistoryType.REPORT -> FireStoreCollections.REPORT.name
        }
    }
}