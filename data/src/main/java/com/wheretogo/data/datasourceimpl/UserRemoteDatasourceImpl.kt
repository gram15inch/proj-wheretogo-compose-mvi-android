package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreTableName
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.name
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRemoteDatasource {
    private val userTable = FireStoreTableName.USER_TABLE.name()
    private val historyTable = FireStoreTableName.HISTORY_TABLE.name()
    private val likeTypeTable = FireStoreTableName.LIKE_TABLE.name()
    private val bookMarkTypeTable = FireStoreTableName.BOOKMARK_TABLE.name()
    private val commentTypeTable = FireStoreTableName.COMMENT_TABLE.name()

    override suspend fun setProfile(profile: Profile): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(profile.uid)
                .set(profile)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun getProfile(uid: String): Profile? {

        return suspendCancellableCoroutine { continuation ->

            firestore.collection(userTable).document(uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null && result.exists()) {
                        val profile = result.toObject(Profile::class.java)
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
        val typeTable = when (wrapper.type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
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
        }

        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(historyTable)
                .document(typeTable)
                .update("historyIdGroup", FieldValue.arrayUnion(historyId))
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getHistoryGroup(uid: String, type: HistoryType): List<String> {
        val typeTable = when (type) {
            HistoryType.LIKE -> likeTypeTable
            HistoryType.BOOKMARK -> bookMarkTypeTable
            HistoryType.COMMENT -> commentTypeTable
        }
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(historyTable)
                .document(typeTable)
                .get()
                .addOnSuccessListener { result ->
                    val data = result.toObject(RemoteHistoryGroupWrapper::class.java)
                    continuation.resume(data?.historyIdGroup ?: emptyList())
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