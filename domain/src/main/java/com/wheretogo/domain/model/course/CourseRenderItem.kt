package com.wheretogo.domain.model.course

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType
import com.wheretogo.domain.usecase.report.ReportContent

enum class StartDirection {
    FORWARD, REVERSE
}

data class CourseRenderItem(
    val courseId: String,
    val uid: String,
    val userName: String,
    val fWaypoint: List<LatLng>,
    val bWaypoint: List<LatLng>,
    val center: LatLng,
    val title: String,
    val type: String,
    val level: String,
    val duration: Leg<Long>,
    val isUserCreate: Boolean,
    val tags: List<String> = emptyList(),
    val direction: StartDirection = StartDirection.FORWARD
) {
    fun toReportContent(reason: ReportReason): ReportContent {
        return ReportContent(
            contentId = courseId,
            contentGroupId = "",
            type = ReportType.COURSE,
            reason = reason,
            targetUserId = uid,
            targetUserName = userName
        )
    }

    companion object {
        val dummy = CourseRenderItem(
            "",
            "",
            "",
            emptyList(),
            emptyList(),
            LatLng(),
            "",
            "",
            "",
            Leg(0L, 0L),
            false
        )
    }
}