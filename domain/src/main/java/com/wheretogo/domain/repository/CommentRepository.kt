package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.Comment

interface CommentRepository {
    suspend fun getComment(groupId: String): List<Comment>
    suspend fun addComment(comment: Comment)
    suspend fun removeComment(comment: Comment)
    suspend fun setCommentGroup(groupId: String, group: List<Comment>)
    fun cacheClear()
}