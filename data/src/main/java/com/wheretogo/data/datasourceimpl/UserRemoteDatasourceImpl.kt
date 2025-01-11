package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.name
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRemoteDatasource {
    private val userTable = FireStoreCollections.USER.name()
    private val historyTable = FireStoreCollections.HISTORY.name()
    private val likeTypeTable = FireStoreCollections.LIKE.name()
    private val bookMarkTypeTable = FireStoreCollections.BOOKMARK.name()
    private val commentTypeTable = FireStoreCollections.COMMENT.name()
    private val reportTable = FireStoreCollections.REPORT.name()
    private val public = FireStoreCollections.PUBLIC.name()
    private val private = FireStoreCollections.PRIVATE.name()


    override suspend fun setProfilePublic(uid: String, profile: ProfilePublic): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(public).document(uid)
                .set(profile)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun setProfilePrivate(uid: String, profile: ProfilePrivate): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(private).document(uid)
                .set(profile)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun getProfilePublic(uid: String): ProfilePublic? {

        return suspendCancellableCoroutine { continuation ->

            firestore.collection(userTable).document(uid).collection(public).document(uid)
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
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun getProfilePrivate(uid: String): ProfilePrivate? {

        return suspendCancellableCoroutine { continuation ->

            firestore.collection(userTable).document(uid).collection(private).document(uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null && result.exists()) {
                        val profile = result.toObject(ProfilePrivate::class.java)
                        if (profile != null) {
                            continuation.resume(profile)
                        } else {
                            continuation.resumeWithException(Exception("User parse error uid:$uid"))
                        }
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Boolean {
        Log.d("tst8", "uid: ${uid}")
        val typeTable = when (wrapper.type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
            HistoryType.REPORT_COMMENT -> reportTable
        }
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(historyTable)
                .document(typeTable)
                .set(wrapper)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Boolean {

        val typeTable = when (type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
            HistoryType.REPORT_COMMENT -> reportTable
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

    override suspend fun getHistoryGroup(uid: String, type: HistoryType): HashSet<String> {
        val typeTable = when (type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
            HistoryType.REPORT_COMMENT -> reportTable
        }
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(historyTable)
                .document(typeTable)
                .get()
                .addOnSuccessListener { result ->
                    val data = result.toObject(RemoteHistoryGroupWrapper::class.java)
                    continuation.resume(data?.historyIdGroup?.toHashSet() ?: hashSetOf())
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }


    suspend fun removeProfile(uid: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid)
                .delete()
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

}