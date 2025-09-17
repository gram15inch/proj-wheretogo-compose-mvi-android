package com.wheretogo.domain.usecase.comment

interface RemoveCommentToCheckPointUseCase {
    suspend operator fun invoke(groupId: String, commentId: String): Result<Unit>
}