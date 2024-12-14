package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasourceimpl.CommentRemoteDatasourceImpl
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.data.toComment
import com.wheretogo.data.toRemoteComment
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val remoteDatasource: CommentRemoteDatasourceImpl,
) : CommentRepository {
    private val _cacheCommentGroup = mutableMapOf<Int, List<Comment>>()
    override suspend fun getComment(groupId: String): List<Comment> {
        return _cacheCommentGroup.getOrPut(groupId.hashCode()) {
            remoteDatasource.getCommentGroupInCheckPoint(groupId)?.remoteCommentGroup?.map { it.toComment() }
                ?: emptyList()
        }
    }

    override suspend fun setComment(groupId: String, group: List<Comment>) {
        remoteDatasource.setCommentGroupInCheckPoint(
            RemoteCommentGroupWrapper(
                groupId,
                group.map { it.toRemoteComment() })
        )
    }

    override fun cacheClear() {
        _cacheCommentGroup.clear()
    }
}