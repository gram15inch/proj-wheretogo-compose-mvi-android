package com.wheretogo.domain.usecase.checkpoint

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointCreateContent

interface AddCheckpointToCourseUseCase {
    suspend operator fun invoke(
        content: CheckPointCreateContent
    ): Result<CheckPoint>
}