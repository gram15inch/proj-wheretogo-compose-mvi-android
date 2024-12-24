package com.wheretogo.domain.model.community

import com.wheretogo.domain.DOMAIN_EMPTY
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType

data class Report(
    val reportId: String = DOMAIN_EMPTY,
    val type: ReportType = ReportType.COMMENT,
    val userId: String = DOMAIN_EMPTY,
    val contentId: String = DOMAIN_EMPTY,
    val targetUserId: String = "",
    val targetUserName: String = "",
    val reason: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val timestamp : Long = 0
)
