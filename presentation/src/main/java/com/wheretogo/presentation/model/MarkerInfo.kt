package com.wheretogo.presentation.model

import androidx.annotation.DrawableRes
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.MarkerType

data class MarkerInfo(
    val contentId: String,
    val position: LatLng? = null,
    val caption: String? = null,
    val type: MarkerType = MarkerType.SPOT,
    @DrawableRes val iconRes: Int? = null,
    val iconPath: String? = null,
    val isVisible: Boolean = true,
    val isHighlight : Boolean = false
)
