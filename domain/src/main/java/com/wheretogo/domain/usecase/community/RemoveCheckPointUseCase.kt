package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse

interface RemoveCheckPointUseCase {
    suspend operator fun invoke(courseId: String, checkPointId: String): UseCaseResponse<String>
}