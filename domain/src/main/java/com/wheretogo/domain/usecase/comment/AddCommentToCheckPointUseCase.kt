package com.wheretogo.domain.usecase.comment

import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentContent

interface AddCommentToCheckPointUseCase {
    suspend operator fun invoke(content: CommentContent): Result<Comment>
}