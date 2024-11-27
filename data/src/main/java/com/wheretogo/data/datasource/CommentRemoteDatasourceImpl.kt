package com.wheretogo.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.CHECKPOINT_TABLE_NAME
import com.wheretogo.data.COMMENT_TABLE_NAME
import com.wheretogo.data.model.comment.RemoteComment
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CommentRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    val checkPointTable = CHECKPOINT_TABLE_NAME
    val commentTable = COMMENT_TABLE_NAME

    suspend fun getCommentGroupInCheckPoint(checkpointId: String): List<RemoteComment> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointTable).document(checkpointId)
                .collection(commentTable)
                .get()
                .addOnSuccessListener {
                    it
                    val group = it.toObjects(RemoteComment::class.java)
                    continuation.resume(group)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    suspend fun addCommentInCheckPoint(checkpointId: String, comment: RemoteComment): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointTable).document(checkpointId)
                .collection(commentTable).document(comment.commentId)
                .set(comment)
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }

    suspend fun removeCommentInCheckPoint(checkpointId: String, commentId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointTable).document(checkpointId)
                .collection(commentTable).document(commentId)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }
}