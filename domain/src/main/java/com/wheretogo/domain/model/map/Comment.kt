package com.wheretogo.domain.model.map

import com.wheretogo.domain.DOMAIN_EMPTY

data class Comment(
    val commentId: String = DOMAIN_EMPTY,
    val userId: String = DOMAIN_EMPTY,
    val groupId: String = DOMAIN_EMPTY,
    val emoji: String = "",
    val detailedReview: String = "",
    val oneLineReview: String = "",
    val date: Long = 0,
    val like: Int = 0
)
