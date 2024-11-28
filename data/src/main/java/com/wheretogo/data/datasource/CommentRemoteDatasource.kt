package com.wheretogo.data.datasource


import com.wheretogo.data.model.comment.RemoteComment

interface CommentRemoteDatasource {

    suspend fun getCommentGroupInCheckPoint(checkpointId: String): List<RemoteComment>

    suspend fun addCommentInCheckPoint(checkpointId: String, comment: RemoteComment): Boolean

    suspend fun removeCommentInCheckPoint(checkpointId: String, commentId: String): Boolean

}