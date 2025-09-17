package com.wheretogo.domain.usecase.comment

import com.wheretogo.domain.model.comment.Comment

interface ReportCommentUseCase {
    suspend operator fun invoke(comment: Comment): Result<String>
}