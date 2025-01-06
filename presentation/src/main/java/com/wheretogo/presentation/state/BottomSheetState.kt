package com.wheretogo.presentation.state

import android.net.Uri
import androidx.compose.ui.focus.FocusRequester
import com.wheretogo.domain.model.community.ImageInfo
import com.wheretogo.presentation.model.MapOverlay

data class BottomSheetState(
    val isVisible: Boolean = false,
    val addMarker: MapOverlay = MapOverlay(),
    val sliderPercent: Float = 0.0f,
    val imgUri: Uri? = null,
    val imgInfo: ImageInfo? = null,
    val description: String = "",
    val focusRequester: FocusRequester = FocusRequester(),
    val error: String = ""
)