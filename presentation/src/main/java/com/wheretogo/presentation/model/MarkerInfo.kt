package com.wheretogo.presentation.model

import androidx.annotation.DrawableRes
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.MarkerType

data class MarkerInfo(
    val contentId: String,
    val position: LatLng? = null,
    val caption: String? = null,
    val type: MarkerType = MarkerType.SPOT,
    @DrawableRes val iconRes: Int? = null,
    val iconPath: String? = null
)
