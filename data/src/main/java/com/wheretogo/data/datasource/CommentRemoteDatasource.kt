package com.wheretogo.data.datasource


import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper

interface CommentRemoteDatasource {

    suspend fun getCommentGroupInCheckPoint(groupId: String): RemoteCommentGroupWrapper?

    suspend fun setCommentGroupInCheckPoint(wrapper: RemoteCommentGroupWrapper): Boolean
}