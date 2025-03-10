package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CourseAddRequest

interface AddCourseUseCase {
    suspend operator fun invoke(courseAddRequset: CourseAddRequest): UseCaseResponse<String>
}