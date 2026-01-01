package com.wheretogo.presentation.intent

import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.HomeBodyBtn

sealed class HomeIntent {
    data class BodyButtonClick(val btn: HomeBodyBtn) : HomeIntent()
    data class LifeCycleChange(val lifeCycle: AppLifecycle) : HomeIntent()
}