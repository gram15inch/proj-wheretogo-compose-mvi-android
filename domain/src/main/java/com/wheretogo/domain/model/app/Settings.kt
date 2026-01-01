package com.wheretogo.domain.model.app

import com.wheretogo.domain.DriveTutorialStep

data class Settings(val tutorialStep: DriveTutorialStep = DriveTutorialStep.SKIP)