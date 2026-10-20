package com.wheretogo.presentation.composable.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dhkim139.core.ui.model.ContentPadding
import timber.log.Timber


@Composable
fun screenSize(isWidth: Boolean): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return if (isWidth) screenWidthDp.dp else screenHeightDp.dp
}

enum class AdMinSize(val widthDp: Int, val heightDp: Int) {
    INVISIBLE(0, 0),
    Row(680, 230),
    Card(370, 340),
    Mini(340, 220)
    ;

    fun fitsIn(width: Dp, height: Dp): Boolean  {
        Timber.d("ad_ ${this.name} ${width >= widthDp.dp && height >= heightDp.dp} : $width >= ${widthDp.dp} && $height >= ${heightDp.dp}")
        return width >= widthDp.dp && height >= heightDp.dp
    }
}

@Composable
fun adSize(contentPadding: ContentPadding = ContentPadding(), cardFirst: Boolean): AdMinSize {
    val configuration = LocalConfiguration.current

    val availableWidth = (configuration.screenWidthDp.dp
            - contentPadding.start - contentPadding.end).coerceAtLeast(0.dp)
    val availableHeight = (configuration.screenHeightDp.dp
            - contentPadding.top - contentPadding.bottom).coerceAtLeast(0.dp)
    println("ad_ width: $availableWidth height: $availableHeight")

    return resolveAdSize(availableWidth, availableHeight, cardFirst)
}

fun resolveAdSize(width: Dp, height: Dp, cardFirst: Boolean): AdMinSize = when {
    cardFirst && AdMinSize.Card.fitsIn(width, height) -> AdMinSize.Card
        AdMinSize.Row.fitsIn(width, height) -> AdMinSize.Row
    !cardFirst && AdMinSize.Card.fitsIn(width, height) -> AdMinSize.Card
    AdMinSize.Mini.fitsIn(width, height) -> AdMinSize.Mini
    else -> AdMinSize.INVISIBLE
}