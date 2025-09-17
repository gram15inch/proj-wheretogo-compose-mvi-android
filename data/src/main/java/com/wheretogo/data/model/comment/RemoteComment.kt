package com.wheretogo.data.model.comment

import com.wheretogo.data.DATA_NULL

data class RemoteComment(
    val commentId: String = DATA_NULL,
    val commentGroupId: String = DATA_NULL,
    val userId: String = DATA_NULL,
    val userName: String = "",
    val emoji: String = "",
    val oneLineReview: String = "",
    val detailedReview: String = "",
    val createAt: Long = 0,
    val like: Int = 0,
    val isFocus: Boolean = false
)