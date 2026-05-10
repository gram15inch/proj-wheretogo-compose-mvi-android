package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType
import com.wheretogo.domain.usecase.report.ReportContent

data class CheckPoint(
    val checkPointId: String = "",
    val courseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val latLng: LatLng = LatLng(),
    val captionId: String = "",
    val caption: String = "",
    val imageId: String = "",
    val thumbnail: String = "",
    val description: String = "",
    val isUserCreated: Boolean = false,
    val isHide: Boolean = false,
    val reportedCount: Int = 0,
    val updateAt: Long = 0L,
    val createAt: Long = 0L
) {
    fun toReportContent(reason: ReportReason): ReportContent {
        return ReportContent(
            contentId = checkPointId,
            contentGroupId = courseId,
            type = ReportType.CHECKPOINT,
            reason = reason,
            targetUserId = userId,
            targetUserName = userName
        )
    }
}