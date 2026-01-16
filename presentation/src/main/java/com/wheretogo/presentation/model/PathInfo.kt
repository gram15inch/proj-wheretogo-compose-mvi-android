package com.wheretogo.presentation.model

import com.wheretogo.domain.PathType
import com.wheretogo.domain.model.address.LatLng

data class PathInfo(
    val contentId: String,
    val type: PathType,
    val points: List<LatLng>,
    val isVisible: Boolean = true
)