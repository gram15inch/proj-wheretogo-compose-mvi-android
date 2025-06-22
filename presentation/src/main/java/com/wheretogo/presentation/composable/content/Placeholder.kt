package com.wheretogo.presentation.composable.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DelayLottieAnimation(modifier: Modifier, ltRes: Int, isVisible: Boolean, delay: Long=0) {
    var shouldShowAnimation by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<Job?>(null) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(ltRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        clipSpec = LottieClipSpec.Progress(0f, 0.4f),
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            if (animation == null)
                animation = launch {
                    delay(delay)
                    shouldShowAnimation = true
                }
        } else {
            animation?.cancel()
            animation = null
            shouldShowAnimation = false
        }
    }
    if (shouldShowAnimation)
        LottieAnimation(
            modifier = modifier,
            composition = composition,
            progress = { progress },
        )
}