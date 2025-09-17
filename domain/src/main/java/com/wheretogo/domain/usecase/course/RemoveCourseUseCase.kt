package com.wheretogo.domain.usecase.course

interface RemoveCourseUseCase{
    suspend operator fun invoke(courseId: String): Result<String>
}