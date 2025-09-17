package com.wheretogo.domain.usecase.checkpoint

import com.wheretogo.domain.model.checkpoint.CheckPoint

interface GetCheckpointForMarkerUseCase {
    suspend operator fun invoke(
        courseId: String,
        forceRefreshIdGroup: List<String> = emptyList()
    ): Result<List<CheckPoint>>
}