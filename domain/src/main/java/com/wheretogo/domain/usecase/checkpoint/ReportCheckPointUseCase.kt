package com.wheretogo.domain.usecase.checkpoint

import com.wheretogo.domain.model.checkpoint.CheckPoint

interface ReportCheckPointUseCase{
    suspend operator fun invoke(
        checkPoint: CheckPoint,
        reason: String
    ): Result<String>
}
