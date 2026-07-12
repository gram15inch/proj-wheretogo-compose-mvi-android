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
import com.wheretogo.presentation.composable.content.rememberMapViewWithLifecycle
import com.wheretogo.presentation.composable.photoviewer.PhotoViewerScreen
import com.wheretogo.presentation.intent.GalleryIntent
import com.wheretogo.presentation.viewmodel.GalleryFlowViewModel


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GalleryFlow(
    viewModel: GalleryFlowViewModel = hiltViewModel(),
) {
    val groupPhoto by viewModel.groupPhoto.collectAsState()
    val gridState = rememberLazyGridState()
    val mapView = rememberMapViewWithLifecycle()
    SharedTransitionLayout(
        modifier = Modifier.navigationBarsPadding(),
    ) {
        AnimatedContent(
            targetState = groupPhoto,
            label = "galleryFlow",
            transitionSpec = {
                fadeIn(tween(300)) togetherWith
                fadeOut(tween(300))
            },
        ) { groupPhoto ->
            if (groupPhoto == null) {
                GalleryScreen(
                    sharedScope = this@SharedTransitionLayout,
                    animatedScope = this@AnimatedContent,
                    gridState = gridState,
                    onPhotoOpen = { viewModel.handleIntent(GalleryIntent.OpenDetail(it.id)) },
                    viewModel = viewModel,
                )
            } else {
                PhotoViewerScreen(
                    photos = groupPhoto.photos,
                    initialIndex = groupPhoto.selectedIndex,
                    sharedScope = this@SharedTransitionLayout,
                    animatedScope = this@AnimatedContent,
                    mapView = mapView,
                    onClose = {
                        viewModel.handleIntent(GalleryIntent.CloseDetail)
                    }
                )
            }
        }
    }
}