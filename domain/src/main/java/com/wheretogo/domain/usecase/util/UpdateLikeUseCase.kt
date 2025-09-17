package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.comment.Comment

interface UpdateLikeUseCase {

    /**
     * @param comment 바꿀 코멘트
     * @param isLike 코멘트의 좋아요가 설정되여야 하는 값 ex) true : 해당 코멘트의 좋아요를 + 로 바꿈
     */
    suspend operator fun invoke(comment: Comment, isLike: Boolean): Result<Unit>
}