package com.wheretogo.domain.model.map

import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.model.address.LatLng

data class MarkerInfo(
    val contentId: String,
    val position: LatLng? = null,
    val caption: String? = null,
    val type: MarkerType = MarkerType.DEFAULT,
    val iconRes: Int? = null,
    val iconPath: String? = null,
    val isVisible: Boolean = true,
    val isHighlight: Boolean = false
)