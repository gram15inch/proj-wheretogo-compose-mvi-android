package com.wheretogo.presentation.feature

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.R
import kotlin.math.max


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ImeStickyBox(
    modifier: Modifier = Modifier,
    onBoxHeightChange: (Dp) -> Unit = {},
    content: @Composable (Dp) -> Unit
) {
    val density = LocalDensity.current
    val navBarBottom = WindowInsets.navigationBars.getBottom(density)
    val imeInsets = WindowInsets.ime
    val imeHeight = max(0f, (imeInsets.getBottom(density) - navBarBottom) / density.density).dp
    Box(modifier = modifier
            .wrapContentHeight()
            .offset(y = -imeHeight)
            .onGloballyPositioned { layoutCoordinates ->
                onBoxHeightChange(with(density) {
                    layoutCoordinates.size.height.dp
                })
            }
    ) {
        content(imeHeight)
    }
}

@Composable
fun BlurEffect(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.gray_C7C7C7_80))
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
    )
}

@Composable
fun Modifier.consumptionEvent(): Modifier {
    return this.pointerInput(Unit) {
        awaitEachGesture { awaitFirstDown().consume() }
    }
}

@Composable
fun Modifier.topShadow(): Modifier {
    return this.graphicsLayer {
        shadowElevation = 8.dp.toPx()
        shape = RectangleShape
        clip = false
    }
}

@Composable
fun screenSize(isWidth: Boolean): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return if (isWidth) screenWidthDp.dp else screenHeightDp.dp
}