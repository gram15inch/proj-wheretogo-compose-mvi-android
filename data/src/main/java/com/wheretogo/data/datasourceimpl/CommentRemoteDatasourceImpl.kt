package com.wheretogo.data.datasourceimpl


import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreTableName
import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CommentRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentRemoteDatasource {
    val checkPointTable = FireStoreTableName.CHECKPOINT_TABLE.name()
    val commentTable = FireStoreTableName.COMMENT_TABLE.name()

    override suspend fun getCommentGroupInCheckPoint(checkpointId: String): List<RemoteComment> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(checkPointTable).document(checkpointId)
                .collection(commentTable)
                .get()
                .addOnSuccessListener {
                    val group = it.toObjects(RemoteComment::class.java)
                    continuation.resume(group)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun addCommentInCheckPoint(
        checkpointId: String,
        comment: RemoteComment
    ): Boolean {
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

    override suspend fun removeCommentInCheckPoint(
        checkpointId: String,
        commentId: String
    ): Boolean {
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