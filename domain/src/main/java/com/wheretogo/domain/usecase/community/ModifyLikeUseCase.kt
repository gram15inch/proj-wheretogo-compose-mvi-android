package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.map.Comment

interface ModifyLikeUseCase {
    suspend operator fun invoke(comment: Comment, isLike: Boolean): Boolean
}
