package com.wheretogo.domain.model.app

data class AppBuildConfig(
    val tmapAppKey: String,
    val isCrashlytics: Boolean,
    val isCoolDown: Boolean,
)