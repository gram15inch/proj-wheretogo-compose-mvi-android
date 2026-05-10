package com.wheretogo.domain.model.comment

import com.wheretogo.domain.DOMAIN_EMPTY
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType
import com.wheretogo.domain.usecase.report.ReportContent

data class Comment(
    val commentId: String = DOMAIN_EMPTY,
    val groupId: String = DOMAIN_EMPTY,
    val userId: String = DOMAIN_EMPTY,
    val userName: String = "",
    val emoji: String = "",
    val oneLineReview: String = "",
    val detailedReview: String = "",
    val like: Int = 0,
    val isUserCreated: Boolean = false,
    val isUserLiked: Boolean = false,
    val isFocus: Boolean = false,
    val isHide: Boolean = false,
    val timestamp: Long = 0,
    val reportedCount: Int = 0,
    val updateAt: Long = 0,
    val createAt: Long = 0,
) {
    fun toReportContent(reason: ReportReason): ReportContent {
        return ReportContent(
            contentId = commentId,
            contentGroupId = groupId,
            type = ReportType.COMMENT,
            reason = reason,
            targetUserId = userId,
            targetUserName = userName
        )
    }
}