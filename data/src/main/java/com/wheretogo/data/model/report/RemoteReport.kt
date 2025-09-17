package com.wheretogo.data.model.report

import com.wheretogo.data.DATA_NULL


data class RemoteReport(
    val reportId: String = DATA_NULL,
    val type: String = DATA_NULL,
    val userId: String = DATA_NULL,
    val contentId: String = DATA_NULL,
    val targetUserId: String = "",
    val targetUserName: String = "",
    val reason: String = "",
    val status: String = "",
    val createAt: Long = 0
)