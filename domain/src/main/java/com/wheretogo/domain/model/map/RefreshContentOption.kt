package com.wheretogo.domain.model.map

enum class ContentOperation {
    REFRESH_COURSES, REFRESH_CLUSTER, DELETE_COURSE, DELETE_CHECKPOINT
}

data class RefreshContentOption(
    val operation: ContentOperation,
    val id: String? = null,
    val groupId: String? = null,
    val cameraState: CameraState? = null
)