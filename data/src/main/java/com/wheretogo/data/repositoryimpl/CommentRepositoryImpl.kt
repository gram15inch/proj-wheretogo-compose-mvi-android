package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.CommentLocalDatasource
import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.di.CommentCache
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.model.content.ContentLikeRequest
import com.wheretogo.data.toComment
import com.wheretogo.data.toCommentGroup
import com.wheretogo.data.toCreateContent
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentAddRequest
import com.wheretogo.domain.repository.CommentRepository
import de.huxhorn.sulky.ulid.ULID
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val remoteDatasource: CommentRemoteDatasource,
    private val localDataSource: CommentLocalDatasource,
    @CommentCache private val cachePolicy: CachePolicy
) : CommentRepository {

    override suspend fun getCommentByGroupId(groupId: String): Result<List<Comment>> {
        val cache = localDataSource.safeGet(groupId)
        val isExpire = cachePolicy.isExpired(
            timestamp = cache.updatedAt,
            isEmpty = cache.commentGroup.isEmpty()
        )
        if (!isExpire)
            cache.commentGroup

        return remoteDatasource.getCommentByGroupId(groupId)
            .mapCatching {
                val commentGroup = it.toCommentGroup()
                localDataSource.safeReplace(groupId, commentGroup, true)
                commentGroup
            }.mapDomainError()
    }

    override suspend fun appendComment(request: CommentAddRequest): Result<Comment> {
        if (request.profile.uid.isBlank())
            return Result.failure(DataError.UserInvalid("잘못된 사용자"))

        val newCommentId = "CM${ULID().nextULID()}"
        val newComment = request.toComment(newCommentId)

        return remoteDatasource.addComment(newComment.toCreateContent()).mapCatching {
            val commentGroup = localDataSource
                .safeGet(newComment.groupId)
                .commentGroup + newComment
            localDataSource.safeReplace(newComment.groupId, commentGroup)
            newComment
        }.mapDomainError()
    }

    override suspend fun removeComment(
        groupId: String,
        commentId: String
    ): Result<Unit> {

        return remoteDatasource.removeComment(groupId, commentId).mapCatching {
            val commentGroup = localDataSource
                .safeGet(groupId)
                .commentGroup.filter { it.commentId != commentId }
            localDataSource.safeReplace(groupId, commentGroup)
        }.mapDomainError()
    }

    override suspend fun changeCommentLike(
        groupId: String,
        commentId: String,
        isLike: Boolean
    ): Result<Unit> {

        return remoteDatasource.changeCommentLike(ContentLikeRequest(groupId, commentId, isLike))
            .mapCatching {
                updateComment(groupId, commentId) {
                    val diff = if (isLike) 1 else -1
                    it.copy(like = it.like + diff)
                }
                Unit
            }.mapDomainError()
    }

    private suspend fun updateComment(
        groupId: String,
        commentId: String,
        newComment: (Comment) -> Comment
    ): Boolean {

        val oldCommentGroup = localDataSource.get(groupId)?.commentGroup
        val oldComment = oldCommentGroup?.find { it.commentId == commentId }
        if (oldCommentGroup == null || oldComment == null)
            return false
        val newCommentGroup = oldCommentGroup.map {
            if (it.commentId == commentId)
                newComment(it)
            else
                it
        }

        localDataSource.safeReplace(groupId, newCommentGroup)
        return true
    }


    override suspend fun clearCache(): Result<Unit> {
        return dataErrorCatching { localDataSource.clear() }.mapDomainError()
    }
}
