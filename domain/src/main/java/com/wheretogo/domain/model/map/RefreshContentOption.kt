package com.wheretogo.domain.model.map

enum class ContentOperation {
    REFRESH_CLUSTER, DELETE_COURSE, DELETE_CHECKPOINT, DELETE_PARKING
}

data class RefreshContentOption(
    val operation: ContentOperation,
    val id: String? = null,
    val groupId: String? = null,
    val cameraState: CameraState? = null
)