package com.wheretogo.domain.usecase.report

import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType

interface ReportContentUseCase{
    suspend operator fun invoke(
        contentId: String,
        contentGroupId: String,
        type: ReportType,
        reason: ReportReason,
        targetUserId: String,
        targetUserName: String
    ): Result<Unit>
}
