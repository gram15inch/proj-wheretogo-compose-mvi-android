package com.wheretogo.presentation.state

import com.wheretogo.domain.DriveTutorialStep

data class GuideState(
    val tutorialStep: DriveTutorialStep = DriveTutorialStep.SKIP,
    val alignment: Align = Align.TOP_START,
    val isHighlight: Boolean = false
) {
    companion object {
        enum class Align {
            TOP_START, BOTTOM_START
        }
    }
}