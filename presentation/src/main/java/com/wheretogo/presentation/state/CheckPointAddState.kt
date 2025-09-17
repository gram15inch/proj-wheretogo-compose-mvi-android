package com.wheretogo.presentation.state

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.ImageInfo

data class CheckPointAddState(
    val latLng: LatLng = LatLng(),
    val sliderPercent: Float = 0.0f,
    val imgUriString: String = "",
    val imgInfo: ImageInfo? = null,
    val description: String = "",
    val isLoading: Boolean = false,
    val isSubmitActive: Boolean = false
)