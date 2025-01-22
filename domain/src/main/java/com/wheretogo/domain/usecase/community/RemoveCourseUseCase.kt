package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse

interface RemoveCourseUseCase {
    suspend operator fun invoke(courseId: String): UseCaseResponse
}