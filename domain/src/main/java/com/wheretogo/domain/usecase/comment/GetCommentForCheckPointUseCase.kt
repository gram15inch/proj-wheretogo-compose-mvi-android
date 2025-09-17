package com.wheretogo.domain.usecase.comment

import com.wheretogo.domain.model.comment.Comment

interface GetCommentForCheckPointUseCase{
    suspend operator fun invoke(groupId: String): Result<List<Comment>>
}