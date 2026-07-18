package com.wheretogo.presentation.composable.content


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.theme.Palette

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    maxDotCount: Int,
    modifier: Modifier = Modifier,
) {
    val half = maxDotCount / 2
    val start = (currentPage - half)
        .coerceIn(0, (pageCount - maxDotCount).coerceAtLeast(0))
    val end = (start + maxDotCount).coerceAtMost(pageCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in start until end) {
            val isCurrent = i == currentPage
            val isEdge = (i == start && start > 0) || (i == end - 1 && end < pageCount)

            val width by animateDpAsState(
                targetValue = if (isCurrent) 18.dp else 6.dp,
                animationSpec = tween(250),
                label = "dot_width"
            )
            val scale by animateFloatAsState(
                targetValue = if (isEdge) 0.6f else 1f,
                animationSpec = tween(250),
                label = "dot_scale"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Palette.White)
            )
        }
    }
}