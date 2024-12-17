package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreTableName
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.comment.RemoteBookmarkGroupWrapper
import com.wheretogo.data.model.comment.RemoteLikeGroupWrapper
import com.wheretogo.data.name
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRemoteDatasource {
    private val userTable = FireStoreTableName.USER_TABLE.name()
    private val likeTable = FireStoreTableName.LIKE_TABLE.name()
    private val bookMarkTable = FireStoreTableName.BOOKMARK_TABLE.name()

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

    override suspend fun getProfileWithLikeAndBookmark(uid: String): Profile? {
        return coroutineScope {
            val profile = async { getProfile(uid) }
            val bookmark = async { getBookmarkGroup(uid) }
            val like = async { getLikeGroup(uid) }
            profile.await()?.copy(
                bookMarkGroup = bookmark.await(),
                likeGroup = like.await()
            )
        }
    }


    override suspend fun setLikeGroup(wrapper: RemoteLikeGroupWrapper): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(wrapper.uid).collection(likeTable)
                .document(wrapper.uid)
                .set(wrapper)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun getLikeGroup(uid: String): List<String> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(likeTable).document(uid)
                .get()
                .addOnSuccessListener { result ->
                    val data = result.toObject(RemoteLikeGroupWrapper::class.java)
                    continuation.resume(data?.likeGroup ?: emptyList())
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun setBookmarkGroup(wrapper: RemoteBookmarkGroupWrapper): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(wrapper.uid).collection(bookMarkTable)
                .document(wrapper.uid)
                .set(wrapper)
                .addOnSuccessListener { result ->
                    continuation.resume(true)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(Exception(e))
                }
        }
    }

    override suspend fun getBookmarkGroup(uid: String): List<String> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(userTable).document(uid).collection(bookMarkTable).document(uid)
                .get()
                .addOnSuccessListener { result ->
                    val data = result.toObject(RemoteBookmarkGroupWrapper::class.java)
                    continuation.resume(data?.bookmarkGroup ?: emptyList())
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