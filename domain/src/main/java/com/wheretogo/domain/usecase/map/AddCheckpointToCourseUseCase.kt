package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CheckPointAddRequest

interface AddCheckpointToCourseUseCase {
    suspend operator fun invoke(
        checkpoint: CheckPointAddRequest
    ): UseCaseResponse<String>
}