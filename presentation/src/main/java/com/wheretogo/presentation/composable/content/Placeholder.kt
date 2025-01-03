package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import com.wheretogo.presentation.R

@Composable
fun ShimmeringPlaceholder() {
    Row(
        modifier = Modifier
            .shimmer()
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.LightGray),
        )
    }
}

@Preview
@Composable
fun ShimmeringPlaceholderPrivet() {
    ShimmeringPlaceholder()
}

@Composable
fun DragHandle(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(16.dp))
                .width(40.dp)
                .height(5.dp)
                .background(colorResource(R.color.gray_C7C7C7_80))
        )
    }
}