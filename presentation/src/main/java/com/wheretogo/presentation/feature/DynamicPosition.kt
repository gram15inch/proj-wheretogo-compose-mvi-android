package com.wheretogo.presentation.feature

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ImeStickyBox(
    modifier: Modifier = Modifier,
    onContainerHeightChange: (Dp) -> Unit,
    content: @Composable (Dp) -> Unit
) {
    val context = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeHeight =
        max(0f, (imeInsets.getBottom(context) / context.density) - 60).dp // 알수없는 키보드 마진 조정 (-60.dp)
    Box(modifier = modifier
        .wrapContentHeight()
        .offset(y = -imeHeight)
        .onGloballyPositioned { layoutCoordinates ->
            onContainerHeightChange(with(context) {
                layoutCoordinates.size.height.toDp()
            })
        }
    ) {
        content(imeHeight)
    }
}