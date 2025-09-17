package com.wheretogo.domain.model.report

import com.wheretogo.domain.DOMAIN_EMPTY
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType

data class ReportAddRequest(
    val type: ReportType = ReportType.COMMENT,
    val userId: String = DOMAIN_EMPTY,
    val contentId: String = DOMAIN_EMPTY,
    val targetUserId: String = "",
    val targetUserName: String = "",
    val reason: String = "",
    val status: ReportStatus = ReportStatus.PENDING
) {
    fun valid(): ReportAddRequest {
        require(contentId.isNotBlank()) { "inValid contentId id" }
        require(userId.isNotBlank()) { "inValid userId id" }
        require(targetUserId.isNotBlank()) { "inValid target userId id" }
        return this
    }
}