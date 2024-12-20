package com.wheretogo.domain.model.map

import com.wheretogo.domain.DOMAIN_EMPTY

data class Comment(
    val commentId: String = DOMAIN_EMPTY,
    val userId: String = DOMAIN_EMPTY,
    val userName: String = "",
    val groupId: String = DOMAIN_EMPTY,
    val emoji: String = "",
    val oneLineReview: String = "",
    val detailedReview: String = "",
    val date: Long = 0,
    val like: Int = 0
)
