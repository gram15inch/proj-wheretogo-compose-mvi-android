package com.wheretogo.data.model.comment

import com.wheretogo.data.DATA_NULL

data class RemoteComment(
    val commentId: String = DATA_NULL,
    val emoji: String = "",
    val oneLineReview: String = "",
    val detailReview: String = ""
)