package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import javax.inject.Inject

class MockCommentRemoteDatasourceImpl @Inject constructor() : CommentRemoteDatasource {
    private val commentGroup= mutableMapOf<String,RemoteCommentGroupWrapper>()
    override suspend fun getCommentGroupInCheckPoint(groupId: String): RemoteCommentGroupWrapper? {
        return commentGroup.get(groupId)
    }

    override suspend fun setCommentGroupInCheckPoint(wrapper: RemoteCommentGroupWrapper): Boolean {
        commentGroup.set(wrapper.groupId, wrapper)
        return true
    }

    override suspend fun setCommentInCheckPoint(comment: RemoteComment, isInit: Boolean): Boolean {
         val wrapper= commentGroup.getOrElse(comment.commentGroupId, { null })
        if (wrapper == null) {
            val wrapper = RemoteCommentGroupWrapper(
                groupId = comment.commentGroupId,
                remoteCommentGroup = listOf(comment)
            )
            commentGroup.put(
                key = comment.commentGroupId,
                value = wrapper
            )
        } else {
            commentGroup.put(
                key = comment.commentGroupId,
                value = wrapper.copy(remoteCommentGroup = wrapper.remoteCommentGroup + comment)
            )
        }

        return true
    }

    override suspend fun updateCommentInCheckPoint(comment: RemoteComment): Boolean {
        val wrapper= commentGroup.getOrElse(comment.commentGroupId, { null })
        if(wrapper==null){
            return false
        }

        commentGroup.put(
            key = comment.commentGroupId,
            value =
                wrapper.copy(
                    groupId = comment.commentGroupId,
                    wrapper.remoteCommentGroup.map {
                        if (it.commentId == comment.commentId)
                            comment
                        else
                            it
                    }
                ))
        return true
    }

    override suspend fun removeCommentGroupInCheckPoint(commentGroupId: String): Boolean {
        val wrapper= commentGroup.getOrElse(commentGroupId, { null })
         if (wrapper == null) {
           return true
        }

        commentGroup.remove(commentGroupId)

        return true
    }
}