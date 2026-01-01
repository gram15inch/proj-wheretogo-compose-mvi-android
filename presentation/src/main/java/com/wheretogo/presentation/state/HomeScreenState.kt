package com.wheretogo.presentation.state

import com.wheretogo.presentation.HomeBodyBtnHighlight

data class HomeScreenState(
    val bodyBtnHighlight: HomeBodyBtnHighlight = HomeBodyBtnHighlight.NONE,
    val guideState: GuideState = GuideState()
)

