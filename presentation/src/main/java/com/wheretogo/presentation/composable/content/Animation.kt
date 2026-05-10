package com.wheretogo.presentation.composable.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class AnimationDirection {
    CenterRight, RightCenter, CenterDown, CenterUp
}

@Composable
fun SlideAnimation(
    modifier: Modifier = Modifier,
    visible: Boolean,
    direction: AnimationDirection,
    content: @Composable () -> Unit
) {
    val enter = when (direction) {
        AnimationDirection.CenterRight -> {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth }
            ) + fadeIn(
                animationSpec = tween(durationMillis = 500)
            )
        }

        AnimationDirection.RightCenter -> {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(
                animationSpec = tween(durationMillis = 500)
            )
        }

        AnimationDirection.CenterDown -> {
            slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 100)
            )
        }

        AnimationDirection.CenterUp -> {
            slideInVertically(
                initialOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 100)
            )
        }

        else -> {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }
            )
        }
    }

    val exit = when (direction) {
        AnimationDirection.CenterRight -> {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        }

        AnimationDirection.RightCenter -> {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        }

        AnimationDirection.CenterDown -> {
            slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(durationMillis = 100)
            )
        }

        AnimationDirection.CenterUp -> {
            slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(durationMillis = 100)
            )
        }


        else -> {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }
            )
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = enter,
        exit = exit
    ) {
        content()
    }
}

@Composable
fun FadeAnimation(
    modifier: Modifier = Modifier,
    visible: Boolean,
    short: Boolean = false,
    content: @Composable () -> Unit
) {
    val fadeIn = fadeIn(animationSpec = tween(durationMillis = if (short) 100 else 250))
    val fadeOut = fadeOut(animationSpec = tween(durationMillis = 50))
    AnimatedContent(
        modifier = modifier,
        targetState = visible,
        transitionSpec = {
            fadeIn togetherWith fadeOut using null
        }
    ) { target ->
        if (target)
            content()
    }
}

@Composable
fun <T> FadeAnimation(
    modifier: Modifier = Modifier,
    state: T? = null,
    short: Boolean = false,
    content: @Composable (T) -> Unit
) {
    val fadeIn = fadeIn(animationSpec = tween(durationMillis = if (short) 100 else 250))
    val fadeOut = fadeOut(animationSpec = tween(durationMillis = 50))
    AnimatedContent(
        modifier = modifier,
        targetState = state,
        transitionSpec = {
            fadeIn togetherWith fadeOut using null
        }
    ) { target ->
        if (target != null)
            content(target)
    }
}