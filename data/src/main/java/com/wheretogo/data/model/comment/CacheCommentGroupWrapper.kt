package com.wheretogo.data.model.comment

import com.wheretogo.domain.model.comment.Comment

data class CacheCommentGroupWrapper(
    val commentGroup: List<Comment> = emptyList(),
    val updatedAt: Long,
)