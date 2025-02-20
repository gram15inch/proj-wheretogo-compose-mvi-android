package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.ProfilePublic
import com.wheretogo.data.name
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.ProfilePrivate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor() : UserRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val userTable = FireStoreCollections.USER.name()
    private val historyTable = FireStoreCollections.HISTORY.name()
    private val likeTypeTable = FireStoreCollections.LIKE.name()
    private val bookMarkTypeTable = FireStoreCollections.BOOKMARK.name()
    private val commentTypeTable = FireStoreCollections.COMMENT.name()
    private val courseTypeTable = FireStoreCollections.COURSE.name()
    private val checkpointTypeTable = FireStoreCollections.CHECKPOINT.name()
    private val reportTable = FireStoreCollections.REPORT.name()
    private val public = FireStoreCollections.PUBLIC.name()
    private val private = FireStoreCollections.PRIVATE.name()


    override suspend fun setProfilePublic(public: ProfilePublic): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(public.uid)
                .set(public)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun setProfilePrivate(uid: String, privateProfile: ProfilePrivate): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(private).document(private)
                .set(privateProfile)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun getProfilePublic(uid: String): ProfilePublic? {

        return suspendCancellableCoroutine { continuation ->

            firestore.collection(userTable).document(uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null && result.exists()) {
                        val profile = result.toObject(ProfilePublic::class.java)
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

    override suspend fun getProfilePublicWithMail(hashMail: String): ProfilePublic? {

        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).whereEqualTo(ProfilePublic::hashMail.name, hashMail)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val profile = result.first().toObject(ProfilePublic::class.java)
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

    override suspend fun getProfilePrivate(userId: String): ProfilePrivate? {

        return suspendCancellableCoroutine { continuation ->

            firestore.collection(userTable).document(userId).collection(private).document(private)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null && result.exists()) {
                        val profile = result.toObject(ProfilePrivate::class.java)
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

    override suspend fun deleteProfile(uid: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val root = firestore.collection(userTable).document(uid)
            root.collection(historyTable).document(bookMarkTypeTable).delete()
            root.collection(historyTable).document(likeTypeTable).delete()
            root.collection(historyTable).document(commentTypeTable).delete()
            root.collection(historyTable).document(courseTypeTable).delete()
            root.collection(historyTable).document(checkpointTypeTable).delete()
            root.collection(private).document(uid).delete()
            root.collection(public).document(uid).delete()
            root.delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }


    override suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Boolean {
        val typeTable = when (wrapper.type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
            HistoryType.COURSE -> courseTypeTable
            HistoryType.CHECKPOINT -> checkpointTypeTable
            HistoryType.REPORT_CONTENT -> reportTable
        }
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(historyTable)
                .document(typeTable)
                .set(wrapper)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Boolean {

        val typeTable = when (type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
            HistoryType.COURSE -> courseTypeTable
            HistoryType.CHECKPOINT -> checkpointTypeTable
            HistoryType.REPORT_CONTENT -> reportTable
        }

        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(historyTable)
                .document(typeTable)
                .update(
                    RemoteHistoryGroupWrapper::historyIdGroup.name,
                    FieldValue.arrayUnion(historyId)
                )
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (e is FirebaseFirestoreException) {
                            setHistoryGroup(uid, RemoteHistoryGroupWrapper(listOf(historyId), type))
                            continuation.resume(true)
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
        val typeTable = when (type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
            HistoryType.COURSE -> courseTypeTable
            HistoryType.CHECKPOINT -> checkpointTypeTable
            HistoryType.REPORT_CONTENT -> reportTable
        }
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(historyTable)
                .document(typeTable)
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

}