package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse

interface ReportCheckPointUseCase {
    suspend operator fun invoke(checkPointId: String, reason: String): UseCaseResponse<String>
}