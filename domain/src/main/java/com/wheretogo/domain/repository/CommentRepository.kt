package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.Comment

interface CommentRepository {
    suspend fun getComment(groupId: String): Result<List<Comment>>
    suspend fun addComment(comment: Comment): Result<Unit>
    suspend fun removeComment(comment: Comment): Result<Unit>
    suspend fun setCommentGroup(groupId: String, group: List<Comment>): Result<Unit>
    fun cacheClear()
}