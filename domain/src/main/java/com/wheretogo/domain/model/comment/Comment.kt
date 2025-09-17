package com.wheretogo.domain.model.comment

import com.wheretogo.domain.DOMAIN_EMPTY

data class Comment(
    val commentId: String = DOMAIN_EMPTY,
    val groupId: String = DOMAIN_EMPTY,
    val userId: String = DOMAIN_EMPTY,
    val userName: String = "",
    val emoji: String = "",
    val oneLineReview: String = "",
    val detailedReview: String = "",
    val like: Int = 0,
    val isUserCreated: Boolean = false,
    val isUserLiked: Boolean = false,
    val isFocus: Boolean = false,
    val createAt: Long = 0,
    val timestamp: Long = 0
)