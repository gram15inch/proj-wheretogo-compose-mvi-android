package com.wheretogo.domain.model.comment

import com.wheretogo.domain.DomainError
import com.wheretogo.domain.model.user.Profile

data class CommentAddRequest(
    val profile: Profile,
    val groupId: String,
    val content: CommentContent
) {
    fun valid(): CommentAddRequest {
        require(profile.uid.isNotBlank()) { throw DomainError.UserInvalid("inValid user id") }
        require(groupId.isNotBlank()) { "inValid groupId id" }
        require(content.oneLineReview.isNotBlank()) { "empty review" }
        return this
    }
}