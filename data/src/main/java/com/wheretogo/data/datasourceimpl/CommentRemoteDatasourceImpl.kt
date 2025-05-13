package com.wheretogo.data.datasourceimpl


import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CommentRemoteDatasourceImpl @Inject constructor() : CommentRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val commentRootCollection = FireStoreCollections.COMMENT.name()
    private val remoteCommentGroupAttr = RemoteCommentGroupWrapper::remoteCommentGroup.name

    override suspend fun getCommentGroupInCheckPoint(groupId: String): RemoteCommentGroupWrapper? {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(commentRootCollection).document(groupId)
                .get()
                .addOnSuccessListener {
                    val data = it.toObject(RemoteCommentGroupWrapper::class.java)
                    continuation.resume(data)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setCommentGroupInCheckPoint(wrapper: RemoteCommentGroupWrapper): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(commentRootCollection).document(wrapper.groupId)
                .set(wrapper)
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setCommentInCheckPoint(comment: RemoteComment, isInit: Boolean): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(commentRootCollection).document(comment.commentGroupId).run {
                if (isInit) {
                    set(RemoteCommentGroupWrapper(comment.commentGroupId, listOf(comment)))
                }
                else {
                    update(
                        remoteCommentGroupAttr,
                        FieldValue.arrayUnion(comment)
                    )
                }.addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
            }

        }
    }

    override suspend fun updateCommentInCheckPoint(comment: RemoteComment): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(commentRootCollection).document(comment.commentGroupId)
                .update(
                    remoteCommentGroupAttr,
                    FieldValue.arrayRemove(comment)
                )
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun removeCommentGroupInCheckPoint(commentGroupId:String):Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(commentRootCollection).document(commentGroupId)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }
}

