package com.wheretogo.presentation.composable.gallery

import androidx.annotation.ArrayRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.theme.WhereTogoTheme
import kotlinx.coroutines.delay

@Composable
fun LoadingMessageScreen(
    modifier: Modifier = Modifier,
    messageIntervalMs: Long = 1400L,
    @ArrayRes messages: Int = R.array.gallery_loading
) {
    val messages = stringArrayResource(messages).toList()
    var messageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(messageIntervalMs) {
        while (true) {
            delay(messageIntervalMs)
            messageIndex = (messageIndex + 1) % messages.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                BouncingDotsIndicator()
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedLoadingMessage(
                    message = messages[messageIndex],
                )
            }
        }
    }
}

@Composable
private fun AnimatedLoadingMessage(message: String) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
    val textColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)

    AnimatedContent(
        targetState = message,
        transitionSpec = {
            slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it } +
                    fadeIn(tween(350)) togetherWith
            slideOutVertically(tween(350, easing = FastOutSlowInEasing)) { -it } +
                    fadeOut(tween(250))
        }
    ) { msg ->
        Message(msg, textStyle, textColor)
    }
}

@Composable
private fun Message(text: String, style: TextStyle, color: Color) {
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BouncingDotsIndicator(
    dotSize: Int = 10,
    dotColor: Color = MaterialTheme.colorScheme.primary
) {
    val transition = rememberInfiniteTransition(label = "bouncingDots")
    val delays = listOf(0, 150, 300)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        delays.forEach { delayMs ->
            val offsetY by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 900
                        0f at delayMs using FastOutSlowInEasing
                        -12f at delayMs + 150 using FastOutSlowInEasing
                        0f at delayMs + 300
                        0f at 900
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            Box(
                modifier = Modifier
                    .size(dotSize.dp)
                    .graphicsLayer { translationY = offsetY.dp.toPx() }
                    .background(dotColor, CircleShape)
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading - Static Message")
@Composable
private fun LoadingScreenPreviewStatic() {
    WhereTogoTheme {
        LoadingMessageScreen()
    }
}