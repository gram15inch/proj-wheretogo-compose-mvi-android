package com.dhkim139.core.ui.screen

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND

enum class MapLayoutType {
    VERTICAL,   // 지도 + 바텀시트
    HORIZONTAL;  // 사이드 패널 + 지도
}

@Composable
fun rememberMapLayoutType(): MapLayoutType {
    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    return remember(sizeClass) {
        val isWideWidth = sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) // 840dp 이상
        val isShortHeight = !sizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND) // 480dp 미만
        if (isWideWidth || isShortHeight) MapLayoutType.HORIZONTAL else MapLayoutType.VERTICAL
    }
}