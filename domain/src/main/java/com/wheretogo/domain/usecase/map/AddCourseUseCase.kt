package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.Course

interface AddCourseUseCase {
    suspend operator fun invoke(course: Course): UseCaseResponse
}