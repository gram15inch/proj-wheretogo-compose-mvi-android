package com.wheretogo.domain.model.report

import com.wheretogo.domain.DOMAIN_EMPTY

data class ReportAddRequest(
    val contentId: String,
    val contentGroupId: String = DOMAIN_EMPTY,
    val type: ReportType,
    val reason: ReportReason = ReportReason.SPAM,
    val targetUserId: String,
    val targetUserName: String = DOMAIN_EMPTY,
    val reporterId: String,
) {
    fun valid(): ReportAddRequest {
        require(contentId.isNotBlank()) { "need contentId" }
        require(reporterId.isNotBlank()) { "need reporterId" }
        require(targetUserId.isNotBlank()) { "need targetUserId" }
        if(type == ReportType.COMMENT)
            require(contentGroupId.isNotBlank()) { "need contentGroupId" }
        return this
    }
}