package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.data.toComment
import com.wheretogo.data.toRemoteComment
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val remoteDatasource: CommentRemoteDatasource,
) : CommentRepository {
    private val _cacheCommentGroup = mutableMapOf<String, List<Comment>>() // 체크포인트id
    override suspend fun getComment(groupId: String): List<Comment> {
        return _cacheCommentGroup.getOrPut(groupId) {
            remoteDatasource.getCommentGroupInCheckPoint(groupId)
                ?.remoteCommentGroup
                ?.map { it.toComment() } ?: emptyList()
        }
    }

    override suspend fun addComment(comment: Comment) {
        val commentGroup = _cacheCommentGroup.getOrPut(comment.groupId) {
            listOf()
        } + comment
        _cacheCommentGroup.put(comment.groupId, commentGroup)
        remoteDatasource.setCommentInCheckPoint(comment)
    }

    override suspend fun setCommentGroup(groupId: String, group: List<Comment>) {
        _cacheCommentGroup.put(groupId, group)
        remoteDatasource.setCommentGroupInCheckPoint(
            RemoteCommentGroupWrapper(
                groupId = groupId,
                remoteCommentGroup = group.map { it.toRemoteComment() })
        )
    }

    override fun cacheClear() {
        _cacheCommentGroup.clear()
    }
}