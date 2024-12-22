package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.Comment

interface ReportCommentUseCase {
    suspend operator fun invoke(comment: Comment): UseCaseResponse
}