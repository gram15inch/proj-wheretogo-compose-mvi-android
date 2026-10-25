package com.dhkim139.core.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

typealias ContentTransitionSpec<S> = AnimatedContentTransitionScope<S>.() -> ContentTransform

object Transitions {

    // Shared Axis X (방향 판단 필요 → 함수)
    fun <S : Comparable<S>> sharedAxisX(): ContentTransitionSpec<S> = {
        val forward = targetState > initialState
        val offset = { full: Int -> if (forward) full / 5 else -full / 5 }
        (slideInHorizontally(tween(300)) { offset(it) } + fadeIn(tween(300)))
            .togetherWith(slideOutHorizontally(tween(300)) { -offset(it) } + fadeOut(tween(150)))
    }
}