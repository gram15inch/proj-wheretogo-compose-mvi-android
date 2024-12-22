package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.map.Comment

interface GetCommentForCheckPointUseCase {
    suspend operator fun invoke(checkPointId: String): List<Comment>
}