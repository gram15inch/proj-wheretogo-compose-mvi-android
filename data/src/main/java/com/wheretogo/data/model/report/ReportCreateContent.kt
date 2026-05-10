package com.wheretogo.data.model.report

data class ReportCreateContent(
    val contentId: String = "",
    val contentGroupId: String = "",
    val type: String = "",
    val reason: String = "",
    val targetUserId: String = "",
    val targetUserName: String = "",
    val userId: String = "",
)
