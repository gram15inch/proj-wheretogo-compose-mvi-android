package com.wheretogo.presentation.composable.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wheretogo.presentation.composable.content.NaverMapSheet
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.viewmodel.test.TestViewModel
import java.io.File

@Composable
fun ImageTestScreen(
    viewModel : TestViewModel = hiltViewModel()
) {
    var imgs by remember { mutableStateOf(emptyList<File>()) }
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        /*var isVisible by remember { mutableStateOf(true) }
        SlideAnimation(
            modifier = Modifier.zIndex(999f).systemBarsPadding()
                .padding(start = 10.dp, top = 40.dp),
            isVisible,
            direction = AnimationDirection.CenterRight
        ) {
            GuidePopup(modifier = Modifier.clickable {
                isVisible = false
            }, GuideState(tutorialStep = state.step))
        }
*/
        NaverMapSheet(
            modifier = Modifier.fillMaxSize(),
            state = state.naverState,
            overlayGroup = state.overlays,
            onCameraUpdate = {
                viewModel.cameraUpdated(it)
            },
            onMarkerClick = {
                //isVisible = true
                viewModel.markerClick()
            }
        )
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp)){
            SearchBar(
                state= SearchBarState(
                    searchBarItemGroup = state.searchBarState.searchBarItemGroup
                ),
                onSearchBarItemClick = {
                    viewModel.searchBarItemClick(it)
                }
            )
        }

    }
}