package com.wheretogo.data.datasource


import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.content.ContentLike

interface CommentRemoteDatasource {

    suspend fun getCommentByGroupId(groupId: String): Result<List<RemoteComment>>

    suspend fun addComment(comment: RemoteComment): Result<Unit>

    suspend fun removeComment(groupId: String, contentId: String): Result<Unit>

    suspend fun changeCommentLike(request: ContentLike): Result<Unit>
}