package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.Course

interface ReportCourseUseCase {
    suspend operator fun invoke(course: Course, reason: String): UseCaseResponse
}