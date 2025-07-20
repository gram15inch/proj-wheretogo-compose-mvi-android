package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.valentinilk.shimmer.shimmer
import com.wheretogo.presentation.theme.White50
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

@Composable
fun RowAdPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(White50)
            .shimmer()
            .fillMaxWidth()
            .height(90.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 60.dp)
                .background(Color.LightGray),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.LightGray),
            )
            Box(
                modifier = Modifier
                    .size(120.dp, 20.dp)
                    .background(Color.LightGray),
            )
        }
    }
}

@Composable
fun CardAdPlaceholder() {
    Column(modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(White50)
        .shimmer()
        .widthIn(max = 500.dp)
        .height(330.dp)
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.LightGray),
            )
            Box(
                modifier = Modifier
                    .size(120.dp, 20.dp)
                    .background(Color.LightGray),
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 600)
@Composable
fun CardAdPlaceHolderPreview(){
    Box(modifier = Modifier.padding(12.dp)){
        CardAdPlaceholder()
    }
}

@Preview(widthDp = 600, heightDp = 350)
@Composable
fun RowAdPlaceHolderPreview(){
    Box(modifier = Modifier.padding(12.dp)) {
        RowAdPlaceholder()
    }
}