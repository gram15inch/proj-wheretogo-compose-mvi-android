package com.wheretogo.domain.model.comment

import com.wheretogo.domain.DOMAIN_EMPTY

data class CommentContent(
    val groupId: String = DOMAIN_EMPTY,
    val emoji: String = "",
    val oneLineReview: String = "",
    val detailedReview: String = "",
    val like: Int = 0
)