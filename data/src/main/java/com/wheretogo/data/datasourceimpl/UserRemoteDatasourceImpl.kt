package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.FirebaseApiService
import com.wheretogo.data.getDbOption
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.data.model.user.UserDeleteRequest
import com.wheretogo.data.name
import com.wheretogo.domain.HistoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor(
    private val firebaseApiService: FirebaseApiService
) : UserRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override suspend fun deleteUser(userId: String, token: String):String{
        return firebaseApiService.deleteUser(
            UserDeleteRequest(userId, getDbOption()),
            "Bearer $token"
        ).body()?.toString()?:""

    }

    override suspend fun setProfilePublic(public: RemoteProfilePublic) {
        return suspendCancellableCoroutine { continuation ->
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

    override suspend fun setProfilePrivate(uid: String, privateProfile: RemoteProfilePrivate) {
        return suspendCancellableCoroutine { continuation ->
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

    override suspend fun getProfilePublic(uid: String): RemoteProfilePublic? {

        return suspendCancellableCoroutine { continuation ->

            firestore.collection(FireStoreCollections.USER.name()).document(uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null && result.exists()) {
                        val profile = result.toObject(RemoteProfilePublic::class.java)
                        if (profile != null) {
                            continuation.resume(profile)
                        } else {
                            continuation.resumeWithException(Exception("User parse error uid:$uid"))
                        }
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun getProfilePublicWithMail(hashMail: String): RemoteProfilePublic? {

        return suspendCancellableCoroutine { continuation ->
            firestore.collection(FireStoreCollections.USER.name())
                .whereEqualTo(RemoteProfilePublic::hashMail.name, hashMail)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val profile = result.first().toObject(RemoteProfilePublic::class.java)
                        if (profile != null) {
                            continuation.resume(profile)
                        } else {
                            continuation.resumeWithException(Exception("User parse error uid:$hashMail"))
                        }
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun getProfilePrivate(userId: String): RemoteProfilePrivate? {

        return suspendCancellableCoroutine { continuation ->

            firestore.collection(FireStoreCollections.USER.name()).document(userId)
                .collection(FireStoreCollections.PRIVATE.name)
                .document(FireStoreCollections.PRIVATE.name)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null && result.exists()) {
                        val profile = result.toObject(RemoteProfilePrivate::class.java)
                        if (profile != null) {
                            continuation.resume(profile)
                        } else {
                            continuation.resumeWithException(Exception("User parse error uid:$userId"))
                        }
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun deleteProfile(uid: String) {
        return suspendCancellableCoroutine { continuation ->
            val root = firestore.collection(FireStoreCollections.USER.name()).document(uid)
            root.collection(FireStoreCollections.HISTORY.name).document(FireStoreCollections.BOOKMARK.name).delete()
            root.collection(FireStoreCollections.HISTORY.name).document(FireStoreCollections.LIKE.name).delete()
            root.collection(FireStoreCollections.HISTORY.name).document(FireStoreCollections.COMMENT.name).delete()
            root.collection(FireStoreCollections.HISTORY.name).document(FireStoreCollections.COURSE.name).delete()
            root.collection(FireStoreCollections.HISTORY.name).document(FireStoreCollections.BOOKMARK.name).delete()
            root.collection(FireStoreCollections.PRIVATE.name).document(uid).delete()
            root.delete()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }


    override suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper) {
        return suspendCancellableCoroutine { continuation ->
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

    override suspend fun deleteHistory(uid: String, type: HistoryType) {
        suspendCancellableCoroutine { continuation ->
            firestore.collection(FireStoreCollections.USER.name()).document(uid)
                .collection(FireStoreCollections.HISTORY.name)
                .document(type.toCollectionName())
                .delete()
                .addOnSuccessListener { _ ->
                    continuation.resume(Unit)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun addHistory(uid: String, historyId: String, type: HistoryType) {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(FireStoreCollections.USER.name()).document(uid)
                .collection(FireStoreCollections.HISTORY.name)
                .document(type.toCollectionName())
                .update(
                    RemoteHistoryGroupWrapper::historyIdGroup.name,
                    FieldValue.arrayUnion(historyId)
                )
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener { e ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (e is FirebaseFirestoreException) {
                            setHistoryGroup(uid, RemoteHistoryGroupWrapper(listOf(historyId), type))
                            continuation.resume(Unit)
                        } else
                            continuation.resumeWithException(e)
                    }
                }
        }
    }

    override suspend fun getHistoryGroup(
        uid: String,
        type: HistoryType
    ): Pair<HistoryType, HashSet<String>> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(FireStoreCollections.USER.name()).document(uid)
                .collection(FireStoreCollections.HISTORY.name)
                .document(type.toCollectionName())
                .get()
                .addOnSuccessListener { result ->
                    val data = result.toObject(RemoteHistoryGroupWrapper::class.java)
                    if (data != null) {
                        val pair = type to data.historyIdGroup.toHashSet()
                        continuation.resume(pair)
                    } else {
                        continuation.resume(type to hashSetOf())
                    }

                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    private fun HistoryType.toCollectionName():String{
        return when(this){
            HistoryType.COURSE-> FireStoreCollections.COURSE.name
            HistoryType.CHECKPOINT-> FireStoreCollections.CHECKPOINT.name
            HistoryType.COMMENT-> FireStoreCollections.COMMENT.name
            HistoryType.LIKE-> FireStoreCollections.LIKE.name
            HistoryType.BOOKMARK-> FireStoreCollections.BOOKMARK.name
            HistoryType.REPORT_CONTENT-> FireStoreCollections.REPORT_CONTENT.name
        }
    }
}