package com.wheretogo.data.datasource


import com.wheretogo.data.model.comment.CacheCommentGroupWrapper
import com.wheretogo.domain.model.comment.Comment

interface CommentLocalDatasource {

    suspend fun get(groupId: String): CacheCommentGroupWrapper?
    suspend fun safeGet(groupId: String): CacheCommentGroupWrapper
    suspend fun safeReplace(
        groupId: String,
        commentGroup: List<Comment>? = null,
        isRefresh: Boolean = false
    )

    suspend fun clear()
}