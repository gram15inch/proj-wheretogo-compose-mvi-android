package com.wheretogo.presentation.composable.gallery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.wheretogo.presentation.viewmodel.GalleryFlowViewModel
import com.wheretogo.presentation.intent.GalleryIntent


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GalleryFlow(
    viewModel: GalleryFlowViewModel = hiltViewModel(),
) {
    val selectedPhoto by viewModel.detailPhoto.collectAsState()
    val gridState = rememberLazyGridState()

    SharedTransitionLayout(
        modifier = Modifier.navigationBarsPadding(),
    ) {
        AnimatedContent(
            targetState = selectedPhoto,
            label = "galleryFlow",
            transitionSpec = {
                fadeIn(tween(300)) togetherWith
                fadeOut(tween(300))
            },
        ) { photo ->
            if (photo == null) {
                GalleryScreen(
                    sharedScope = this@SharedTransitionLayout,
                    animatedScope = this@AnimatedContent,
                    gridState = gridState,
                    onPhotoOpen = { viewModel.handleIntent(GalleryIntent.OpenDetail(it.id)) },
                    viewModel = viewModel,
                )
            } else {
                PhotoViewerScreen(
                    photo = photo,
                    sharedScope = this@SharedTransitionLayout,
                    animatedScope = this@AnimatedContent,
                    onClose = { viewModel.handleIntent(GalleryIntent.CloseDetail) },
                )
            }
        }
    }
}