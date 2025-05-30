package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.data.toComment
import com.wheretogo.data.toRemoteComment
import com.wheretogo.domain.UserNotExistException
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val remoteDatasource: CommentRemoteDatasource,
) : CommentRepository {
    private val _cacheCommentGroup = mutableMapOf<String, List<Comment>>() // 체크포인트id
    override suspend fun getComment(groupId: String): Result<List<Comment>> {
        return runCatching {
            remoteDatasource.getCommentGroupInCheckPoint(groupId)
                ?.remoteCommentGroup
                ?.map { it.toComment() }?.apply {
                    _cacheCommentGroup[groupId] = this
                } ?: emptyList()
        }
    }

    override suspend fun addComment(comment: Comment): Result<Unit> {
        return runCatching {
            comment.userId.isEmpty()
                .let { if (it) throw UserNotExistException("inValid userId: ${comment.userId}") }
            require(comment.groupId.isNotEmpty()) { "inValid groupId: ${comment.groupId}" }
            val commentGroup = _cacheCommentGroup.getOrPut(comment.groupId) {
                listOf()
            } + comment
            remoteDatasource.setCommentInCheckPoint(
                comment.toRemoteComment(),
                commentGroup.size == 1
            )
            _cacheCommentGroup.put(comment.groupId, commentGroup)
        }
    }

    override suspend fun removeComment(comment: Comment): Result<Unit> {
        return runCatching {
            val commentGroup = _cacheCommentGroup.getOrPut(comment.groupId) {
                listOf()
            } - comment
            remoteDatasource.updateCommentInCheckPoint(comment.toRemoteComment())
            _cacheCommentGroup.put(comment.groupId, commentGroup)
        }
    }

    override suspend fun removeCommentGroup(groupId: String): Result<Unit> {
        return runCatching {
            remoteDatasource.removeCommentGroupInCheckPoint(groupId)
            _cacheCommentGroup.remove(groupId)
        }
    }

    override suspend fun setCommentGroup(groupId: String, group: List<Comment>): Result<Unit> {
        return runCatching {
            remoteDatasource.setCommentGroupInCheckPoint(
                RemoteCommentGroupWrapper(
                    groupId = groupId,
                    remoteCommentGroup = group.map { it.toRemoteComment() })
            )
            _cacheCommentGroup.put(groupId, group)
        }
    }
}