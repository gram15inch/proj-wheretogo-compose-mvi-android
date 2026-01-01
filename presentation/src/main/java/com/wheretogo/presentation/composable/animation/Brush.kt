package com.wheretogo.presentation.composable.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.theme.Blue50
import com.wheretogo.presentation.theme.Green50

@Composable
fun highlightsBrush(durationMils: Int = 500, colors: List<Color> = listOf(Blue50, Green50)): Brush {
    val transition = rememberInfiniteTransition(label = "gradient-shine")
    val offsetX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(delayMillis = durationMils, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(offsetX, 0f),
        end = Offset(offsetX + 300f, 300f)
    )
    return brush
}

@Composable
fun Modifier.highlightRoundedCorner(
    isVisible: Boolean,
    width: Dp = 6.dp,
    size: Dp = 4.dp
): Modifier {
    return if (isVisible) {
        val brush = highlightsBrush()
        this.border(width, brush = brush, shape = RoundedCornerShape(size))
    } else
        this
}

@Composable
fun Modifier.highlightCircle(isVisible: Boolean, width: Dp = 6.dp): Modifier {
    return if (isVisible) {
        val brush = highlightsBrush()
        this.border(width, brush = brush, shape = CircleShape)
    } else this
}