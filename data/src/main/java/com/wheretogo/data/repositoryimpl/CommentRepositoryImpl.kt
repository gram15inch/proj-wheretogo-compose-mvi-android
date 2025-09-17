package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.di.CommentCache
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.model.comment.CacheCommentGroupWrapper
import com.wheretogo.data.model.content.ContentLike
import com.wheretogo.data.toComment
import com.wheretogo.data.toCommentGroup
import com.wheretogo.data.toRemoteComment
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentAddRequest
import com.wheretogo.domain.repository.CommentRepository
import de.huxhorn.sulky.ulid.ULID
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val remoteDatasource: CommentRemoteDatasource,
    @CommentCache private val cachePolicy: CachePolicy
) : CommentRepository {
    private val _cacheByGroupId = mutableMapOf<String, CacheCommentGroupWrapper>() // 체크포인트 그룹id

    override suspend fun getCommentByGroupId(groupId: String): Result<List<Comment>> {
        val cache = _cacheByGroupId.safeGet(groupId)
        val isExpire = cachePolicy.isExpired(
            timestamp = cache.updatedAt,
            isEmpty = cache.commentGroup.isEmpty()
        )
        if (!isExpire)
            cache.commentGroup

        return remoteDatasource.getCommentByGroupId(groupId)
            .mapCatching {
                val commentGroup = it.toCommentGroup()
                _cacheByGroupId.safeReplace(groupId, commentGroup, true)
                commentGroup
            }.mapDomainError()
    }

    override suspend fun appendComment(request: CommentAddRequest): Result<Comment> {
        if (request.profile.uid.isBlank())
            return Result.failure(DataError.UserInvalid("잘못된 사용자"))

        val newCommentId = "CM${ULID().nextULID()}"
        val newComment = request.toComment(newCommentId)

        return remoteDatasource.addComment(newComment.toRemoteComment()).mapCatching {
            val commentGroup = _cacheByGroupId
                .safeGet(newComment.groupId)
                .commentGroup + newComment
            _cacheByGroupId.safeReplace(newComment.groupId, commentGroup)
            newComment
        }.mapDomainError()
    }

    override suspend fun removeComment(
        groupId: String,
        commentId: String
    ): Result<Unit> {

        return remoteDatasource.removeComment(groupId, commentId).mapCatching {
            val commentGroup = _cacheByGroupId
                .safeGet(groupId)
                .commentGroup.filter { it.commentId != commentId }
            _cacheByGroupId.safeReplace(groupId, commentGroup)
        }.mapDomainError()
    }

    override suspend fun changeCommentLike(
        groupId: String,
        commentId: String,
        isLike: Boolean
    ): Result<Unit> {

        return remoteDatasource.changeCommentLike(ContentLike(groupId, commentId, isLike))
            .mapCatching {
                updateComment(groupId, commentId) {
                    val diff = if (isLike) 1 else -1
                    it.copy(like = it.like + diff)
                }
                Unit
            }.mapDomainError()
    }

    private fun updateComment(
        groupId: String,
        commentId: String,
        newComment: (Comment) -> Comment
    ): Boolean {

        val oldCommentGroup = _cacheByGroupId[groupId]?.commentGroup
        val oldComment = oldCommentGroup?.find { it.commentId == commentId }
        if (oldCommentGroup == null || oldComment == null)
            return false
        val newCommentGroup = oldCommentGroup.map {
            if (it.commentId == commentId)
                newComment(it)
            else
                it
        }

        _cacheByGroupId.safeReplace(groupId, newCommentGroup)
        return true
    }

    private fun MutableMap<String, CacheCommentGroupWrapper>.safeGet(groupId: String): CacheCommentGroupWrapper {
        return getOrPut(groupId) { createWrapper() }
    }

    private fun MutableMap<String, CacheCommentGroupWrapper>.safeReplace(
        groupId: String,
        commentGroup: List<Comment>? = null,
        isRefresh: Boolean = false
    ) {
        val timestamp = if (isRefresh) System.currentTimeMillis() else safeGet(groupId).updatedAt
        put(
            groupId, CacheCommentGroupWrapper(
                commentGroup = commentGroup ?: emptyList(),
                updatedAt = timestamp
            )
        )
    }

    private fun createWrapper(commentGroup: List<Comment>? = null): CacheCommentGroupWrapper {
        val timestamp = if (commentGroup != null) System.currentTimeMillis() else 0
        return CacheCommentGroupWrapper(
            commentGroup = commentGroup ?: emptyList(),
            updatedAt = timestamp
        )
    }

    override suspend fun clearCache(): Result<Unit> {
        return dataErrorCatching { _cacheByGroupId.clear() }.mapDomainError()
    }
}
