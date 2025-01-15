package com.wheretogo.data.datasource


import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper

interface CommentRemoteDatasource {

    suspend fun getCommentGroupInCheckPoint(groupId: String): RemoteCommentGroupWrapper?

    suspend fun setCommentGroupInCheckPoint(wrapper: RemoteCommentGroupWrapper): Boolean

    suspend fun setCommentInCheckPoint(comment: RemoteComment, isInit: Boolean): Boolean

    suspend fun removeCommentInCheckPoint(comment: RemoteComment): Boolean
}