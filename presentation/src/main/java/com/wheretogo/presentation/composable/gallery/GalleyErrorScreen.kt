package com.wheretogo.presentation.composable.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wheretogo.presentation.R

@Composable
fun GalleryErrorScreen(message: String, onRetryButtonClick:()->Unit) {
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message)
            TextButton(
                onClick =  onRetryButtonClick
            ) { Text(stringResource(R.string.retry)) }
        }
    }
}