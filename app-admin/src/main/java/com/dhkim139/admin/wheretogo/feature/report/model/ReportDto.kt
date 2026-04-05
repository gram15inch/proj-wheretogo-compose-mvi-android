package com.dhkim139.admin.wheretogo.feature.report.model

import com.squareup.moshi.JsonClass


// response
@JsonClass(generateAdapter = true)
data class ReportDto(
    val id: String,
    val contentId: String,
    val contentGroupId: String?,
    val type: String,
    val reason: String,
    val status: String,
    val targetUserId: String,
    val targetUserName: String,
    val userId: String,
    val moderate: String,
    val createAt: Long,
) {
    fun toDomain() = Report(
        id = id,
        contentId = contentId,
        contentGroupId = contentGroupId,
        type = type,
        reason = reason,
        status = ReportStatus.from(status),
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        userId = userId,
        moderate = ModerateSeverity.from(moderate),
        createAt = createAt,
    )
}

