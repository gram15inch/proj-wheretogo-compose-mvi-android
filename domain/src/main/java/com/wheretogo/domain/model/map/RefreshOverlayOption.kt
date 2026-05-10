package com.wheretogo.domain.model.map

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course

enum class OverlayOperation {
    CREATE, UPDATE, DELETE
}

data class RefreshOverlayOption(
    val operation: OverlayOperation,
    val markerId: String? = null,
    val courseId: String? = null,
    val checkPointId: String? = null,
    val course: Course? = null,
    val checkPoint: CheckPoint? = null,
    val markerInfo: MarkerInfo? = null,
)