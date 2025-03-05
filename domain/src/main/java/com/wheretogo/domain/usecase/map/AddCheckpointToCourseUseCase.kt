package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CheckPointAddRequest

interface AddCheckpointToCourseUseCase {
    suspend operator fun invoke(
        checkpointAddRequest: CheckPointAddRequest
    ): UseCaseResponse<String>
}