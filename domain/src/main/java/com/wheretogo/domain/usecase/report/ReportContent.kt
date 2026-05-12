package com.wheretogo.domain.usecase.report

import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType

data class ReportContent(
    val contentId: String,
    val contentGroupId: String,
    val type: ReportType,
    val reason: ReportReason,
    val targetUserId: String,
    val targetUserName: String
)