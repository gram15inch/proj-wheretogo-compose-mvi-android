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
fun adSize(isKeyboardUp: Boolean= false): AdMinSize {
    val configuration = LocalConfiguration.current
    val ratio = configuration.screenWidthDp/configuration.screenHeightDp.toFloat()
    val widthDp = configuration.screenWidthDp
    val heightDp = when{
        ratio<0.65f -> configuration.screenHeightDp
        isKeyboardUp -> (configuration.screenHeightDp * 0.6f).toInt()
        else -> configuration.screenHeightDp
    }
    val isCard = widthDp >= AdMinSize.Card.widthDp && heightDp >= AdMinSize.Card.heightDp
    val isRow = widthDp >= AdMinSize.Row.widthDp && heightDp >= AdMinSize.Row.heightDp
    return when{
        isCard-> AdMinSize.Card
        isRow->  AdMinSize.Row
        else-> AdMinSize.INVISIBLE
    }
}
