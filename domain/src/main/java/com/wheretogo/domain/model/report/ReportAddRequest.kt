package com.wheretogo.domain.model.report

import com.wheretogo.domain.DOMAIN_EMPTY
import com.wheretogo.domain.DomainError

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
        require(contentId.isNotBlank()) { "need contentId: $contentId" }
        require(reporterId.isNotBlank())
        { throw DomainError.UserInvalid("need targetUserId: $targetUserId") }
        require(targetUserId.isNotBlank()) { }
        if (type == ReportType.COMMENT)
            require(contentGroupId.isNotBlank()) { "need contentGroupId: $contentGroupId" }
        return this
    }
}