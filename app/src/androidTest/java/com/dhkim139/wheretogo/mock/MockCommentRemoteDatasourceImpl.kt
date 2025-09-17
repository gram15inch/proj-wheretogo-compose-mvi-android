package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.content.ContentLike
import javax.inject.Inject

class MockCommentRemoteDatasourceImpl @Inject constructor() : CommentRemoteDatasource {
    private val _commentGroup = mutableMapOf<String, List<RemoteComment>>()

    override suspend fun getCommentByGroupId(groupId: String): Result<List<RemoteComment>> {
        val data = _commentGroup.getOrDefault(groupId, emptyList())
        return Result.success(data)
    }

    override suspend fun addComment(comment: RemoteComment): Result<Unit> {
        val data =
            _commentGroup.getOrDefault(comment.commentGroupId, emptyList()) + comment
        _commentGroup.put(comment.commentGroupId, data)
        return Result.success(Unit)
    }

    override suspend fun removeComment(
        groupId: String,
        contentId: String
    ): Result<Unit> {
        val data =
            _commentGroup.getOrDefault(groupId, emptyList())
                .filter { it.commentId != contentId }
        _commentGroup.put(groupId, data)
        return Result.success(Unit)
    }

    override suspend fun changeCommentLike(request: ContentLike): Result<Unit> {
        val diff = if (request.isLike) 1 else -1
        val data =
            _commentGroup.getOrDefault(request.groupId, emptyList())
                .map {
                    if (it.commentId == request.contentId)
                        it.copy(like = it.like + diff)
                    else it
                }
        _commentGroup.put(request.groupId, data)
        return Result.success(Unit)
    }
}