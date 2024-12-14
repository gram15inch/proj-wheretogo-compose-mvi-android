package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.Comment

interface CommentRepository {
    suspend fun getComment(groupId: String): List<Comment>
    suspend fun setComment(groupId: String, group: List<Comment>)
    fun cacheClear()
}