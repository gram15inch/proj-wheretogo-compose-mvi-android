package com.wheretogo.presentation.composable.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.AdMinSize


@Composable
fun screenSize(isWidth: Boolean): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return if (isWidth) screenWidthDp.dp else screenHeightDp.dp
}

@Composable
fun adSize(): AdMinSize {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp

    return when{
        widthDp >= AdMinSize.Card.widthDp && heightDp >= AdMinSize.Card.heightDp-> AdMinSize.Card
        widthDp >= AdMinSize.Row.widthDp && heightDp >= AdMinSize.Row.heightDp-> AdMinSize.Row
        else-> AdMinSize.INVISIBLE

    }
}
