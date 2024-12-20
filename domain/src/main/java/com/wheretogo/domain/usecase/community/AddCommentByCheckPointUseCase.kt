package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.map.Comment

interface AddCommentByCheckPointUseCase {
    suspend operator fun invoke(comment: Comment)
}