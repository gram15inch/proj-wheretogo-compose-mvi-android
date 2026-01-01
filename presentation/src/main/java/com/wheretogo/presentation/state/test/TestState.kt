package com.wheretogo.presentation.state.test

import com.naver.maps.map.clustering.Clusterer
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.NaverMapState
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.viewmodel.test.ItemKey

data class TestState(
    val naverState: NaverMapState = NaverMapState(),
    val overlays: List<MapOverlay> = emptyList(),
    val clustererGroup: List<Clusterer<ItemKey>> = emptyList(),
    val step: DriveTutorialStep = DriveTutorialStep.SKIP,
    val searchBarState: SearchBarState = SearchBarState()
)
