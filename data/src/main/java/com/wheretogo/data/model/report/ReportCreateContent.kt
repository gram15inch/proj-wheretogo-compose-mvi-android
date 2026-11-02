package com.wheretogo.data.model.report

import kotlinx.serialization.Serializable

@Serializable
data class ReportCreateContent(
    val contentId: String = "",
    val contentGroupId: String = "",
    val type: String = "",
    val reason: String = "",
    val targetUserId: String = "",
    val targetUserName: String = "",
    val userId: String = "",
)
