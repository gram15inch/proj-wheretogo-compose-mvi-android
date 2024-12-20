package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.map.Comment

interface GetCommentByCheckPointUseCase {
    suspend operator fun invoke(checkPointId: String): List<Comment>
}