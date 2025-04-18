package com.wheretogo.presentation.state

import android.net.Uri
import androidx.compose.ui.focus.FocusRequester
import com.wheretogo.domain.model.community.ImageInfo
import com.wheretogo.domain.model.map.LatLng

data class CheckPointAddState(
    val latlng: LatLng = LatLng(),
    val sliderPercent: Float = 0.0f,
    val imgUri: Uri? = null,
    val imgInfo: ImageInfo? = null,
    val description: String = "",
    val isLoading: Boolean = false,
    val isSubmitActive: Boolean = false,
    val focusRequester: FocusRequester = FocusRequester(),
    val error: String = ""
)