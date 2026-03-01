package com.wheretogo.data.model.report


data class ReportResponse(
    val reportId: String = "",
    val contentId: String = "",
    val contentGroupId: String = "",
    val type: String = "",
    val reason: String = "",
    val status: String = "",
    val userId: String = "",
    val targetUserId: String = "",
    val targetUserName: String = "",
    val moderate: String = "",
    val createAt: Long = 0L
)