package com.wheretogo.domain.model.comment

import com.wheretogo.domain.model.user.Profile

data class CommentAddRequest(
    val profile: Profile,
    val content: CommentContent
) {
    fun valid(): CommentAddRequest {
        require(profile.uid.isNotBlank()) { "inValid userId id" }
        require(content.groupId.isNotBlank()) { "inValid groupId id" }
        require(content.oneLineReview.isNotBlank()) { "empty review" }
        return this
    }
}