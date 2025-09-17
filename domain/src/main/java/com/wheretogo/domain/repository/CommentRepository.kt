package com.wheretogo.domain.repository

import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentAddRequest

interface CommentRepository {
    suspend fun getCommentByGroupId(groupId: String): Result<List<Comment>>

    suspend fun appendComment(request: CommentAddRequest): Result<Comment>

    suspend fun removeComment(groupId: String, commentId: String): Result<Unit>

    suspend fun changeCommentLike(groupId: String, commentId: String, isLike: Boolean): Result<Unit>

    suspend fun clearCache(): Result<Unit>
}