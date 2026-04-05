package com.dhkim139.admin.wheretogo.feature.report.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportActionRequest(
    val reportId: String,
    val actions: List<String>,
    val duration: Int? = null,
    val reason: String? = null,
)

enum class ReportAction {
    SUSPEND_WRITER, SUSPEND_REPORTER, HIDE_CONTENT, SHOW_CONTENT, APPROVE, REJECT
}