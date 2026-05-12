package com.wheretogo.presentation.state

import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.presentation.model.MapOverlay

data class MapState(
    val naverMapState: NaverMapState = NaverMapState(),
    val tutorialStep: DriveTutorialStep = DriveTutorialStep.SKIP,
    val isOverlayLoading: Boolean = false,
    val overlayGroup: List<MapOverlay> = emptyList(),
    val fingerPrint: Int = 0
)