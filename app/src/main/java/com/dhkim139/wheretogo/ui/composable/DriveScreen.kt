package com.dhkim139.wheretogo.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DriveContent(displayMaxWidth: Dp) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .fillMaxHeight()
        ,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopBar(displayMaxWidth)
        NaverScreen(displayMaxWidth)
        BottomBar(displayMaxWidth)
    }
}




