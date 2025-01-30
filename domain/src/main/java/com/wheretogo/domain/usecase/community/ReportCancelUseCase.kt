package com.wheretogo.domain.usecase.community

import com.wheretogo.domain.model.UseCaseResponse

interface ReportCancelUseCase {
    suspend operator fun invoke(reportId: String, reason: String): UseCaseResponse<String>
}