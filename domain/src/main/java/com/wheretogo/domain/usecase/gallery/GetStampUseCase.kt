package com.wheretogo.domain.usecase.gallery

import com.wheretogo.domain.model.gallery.Stamp

interface GetStampUseCase {
    suspend operator fun invoke(courseId: String): Result<Stamp?>
    suspend fun getGroupByCourseId(): Result<Map<String, HashSet<String>>>
}