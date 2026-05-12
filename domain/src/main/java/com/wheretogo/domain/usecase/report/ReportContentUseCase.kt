package com.wheretogo.domain.usecase.report

import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType

interface ReportContentUseCase {
    suspend operator fun invoke(
        content: ReportContent
    ): Result<Unit>

    suspend fun bySelect(type: ReportType, reason: ReportReason): Result<Unit>
}

