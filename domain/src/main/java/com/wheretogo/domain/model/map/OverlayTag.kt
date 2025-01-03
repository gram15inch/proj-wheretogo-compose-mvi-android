package com.wheretogo.domain.model.map

import com.wheretogo.domain.OverlayType

data class OverlayTag(
    val overlayId: String,
    val parentId: String = "",
    val type: OverlayType = OverlayType.NONE
)