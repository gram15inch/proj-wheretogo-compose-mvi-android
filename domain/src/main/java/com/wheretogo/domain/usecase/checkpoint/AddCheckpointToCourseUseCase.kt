package com.wheretogo.domain.usecase.checkpoint

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointContent

interface AddCheckpointToCourseUseCase{
    suspend operator fun invoke(
        content: CheckPointContent
    ): Result<CheckPoint>
}