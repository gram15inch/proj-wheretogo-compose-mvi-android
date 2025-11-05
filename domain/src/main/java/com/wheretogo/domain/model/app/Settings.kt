package com.wheretogo.domain.model.app

import com.wheretogo.domain.TutorialStep

data class Settings(val tutorialStep: TutorialStep = TutorialStep.SKIP)