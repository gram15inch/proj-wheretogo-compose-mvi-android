package com.wheretogo.data.datasource


import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.domain.model.map.Comment

interface CommentRemoteDatasource {

    suspend fun getCommentGroupInCheckPoint(groupId: String): RemoteCommentGroupWrapper?

    suspend fun setCommentGroupInCheckPoint(wrapper: RemoteCommentGroupWrapper): Boolean

    suspend fun setCommentInCheckPoint(comment: Comment): Boolean

    suspend fun removeCommentInCheckPoint(comment: Comment): Boolean
}