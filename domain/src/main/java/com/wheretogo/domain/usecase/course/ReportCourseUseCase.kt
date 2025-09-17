package com.wheretogo.domain.usecase.course

import com.wheretogo.domain.model.course.Course

interface ReportCourseUseCase {
    suspend operator fun invoke(course: Course, reason: String): Result<String>
}