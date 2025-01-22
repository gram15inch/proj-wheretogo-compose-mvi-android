package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CheckPoint

interface ReportCheckPointUseCase {
    suspend operator fun invoke(checkPoint: CheckPoint, reason: String): UseCaseResponse
}