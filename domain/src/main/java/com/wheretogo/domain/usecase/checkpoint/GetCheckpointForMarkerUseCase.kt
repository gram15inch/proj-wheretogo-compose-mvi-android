package com.wheretogo.domain.usecase.checkpoint

import com.wheretogo.domain.model.checkpoint.CheckPoint

interface GetCheckpointForMarkerUseCase {
    suspend operator fun invoke(courseId: String): Result<List<CheckPoint>>
}