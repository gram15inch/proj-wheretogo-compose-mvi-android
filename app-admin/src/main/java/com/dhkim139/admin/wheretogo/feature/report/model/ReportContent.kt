package com.dhkim139.admin.wheretogo.feature.report.model

data class ReportContent(
    val text: String,
    val subText: String? = null,
    val imgUrl: String? = null,
    val isHide: Boolean,
    val reportedCount: Int,
    val likeCount: Int,
    val createAt: Long,
    val updateAt: Long
)

