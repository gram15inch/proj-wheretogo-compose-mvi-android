package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.toMarkerTag
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.FloatingButtons
import com.wheretogo.presentation.composable.content.MapPopup
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.viewmodel.DriveViewModel

@Composable
fun DriveScreen(navController: NavController, viewModel: DriveViewModel = hiltViewModel()) {
    val state by viewModel.driveScreenState.collectAsState()
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    val isWideSize = screenSize(true) > 650.dp
    BackHandler {
        navController.navigateUp()
    }

    Column(modifier = Modifier.zIndex(1f)) {
        Text("${state.mapState.mapData.size}", fontSize = 50.sp)
    }

    NaverMap(
        modifier = Modifier.zIndex(0f),
        overlayMap = state.mapState.mapData,
        onMapAsync = { map ->
            naverMap = map
            viewModel.handleIntent(DriveScreenIntent.MapIsReady)
        },
        onLocationMove = { latLng ->
            viewModel.handleIntent(DriveScreenIntent.UpdateLocation(latLng))
        },
        onCameraMove = { latLng, vp ->
            viewModel.handleIntent(DriveScreenIntent.UpdateCamera(latLng, vp))
        },
        onCourseMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CourseMarkerClick(marker.tag!!.toMarkerTag()))
        },
        onCheckPointMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CheckPointMarkerClick(marker.tag!!.toMarkerTag()))
        }
    )

    FadeAnimation(visible = state.popUpState.isVisible) {
        BlurEffect(onClick = {
            viewModel.handleIntent(DriveScreenIntent.DismissPopup)
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        FadeAnimation(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(horizontal = 12.dp),
            visible = state.listState.isVisible
        ) {
            OneHandArea {
                DriveList(
                    modifier = Modifier.align(alignment = Alignment.BottomEnd),
                    listItemGroup = state.listState.listItemGroup,
                    onItemClick = { selectedItem ->
                        viewModel.handleIntent(DriveScreenIntent.DriveListItemClick(selectedItem))
                    },
                    onBookmarkClick = {
                        viewModel.handleIntent(DriveScreenIntent.DriveListItemBookmarkClick(it))
                    }
                )
            }
        }

        FadeAnimation(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(6.dp),
            visible = state.popUpState.isVisible
        ) {
            OneHandArea {
                MapPopup(
                    modifier = Modifier.align(Alignment.BottomStart),
                    data = state.popUpState.commentState.data,
                    imageUrl = state.popUpState.imageUrl,
                    isWideSize = isWideSize,
                    isCommentVisible = state.popUpState.isCommentVisible,
                    onCommentFloatingButtonClick = {
                        viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)
                    },
                    onCommentListItemClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(item))
                    },
                    onCommentLikeClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentLikeClick(item))
                    }
                )
            }
        }

        FloatingButtons(
            modifier = Modifier.fillMaxSize(),
            course = state.listState.clickItem.course,
            isCommentVisible = state.floatingButtonState.isCommentVisible,
            isExportVisible = state.floatingButtonState.isExportVisible,
            isFoldVisible = state.floatingButtonState.isFoldVisible,
            isExportBackPlate = state.floatingButtonState.isBackPlateVisible,
            onCommentClick = {
                viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)
            },
            onExportMapClick = {
                viewModel.handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick)
            },
            onFoldClick = {
                viewModel.handleIntent(DriveScreenIntent.FoldFloatingButtonClick)
            }
        )
    }

}

//미리보기


@Composable
fun OneHandArea(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(650.dp)
    ) {
        content()
    }
}

@Composable
fun BlurEffect(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.gray_50))
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
    )
}

@Composable
fun ExtendArea(
    isExtend: Boolean,
    holdContent: @Composable () -> Unit,
    moveContent: @Composable () -> Unit
) {
    if (isExtend) {
        Row() {
            holdContent()
            moveContent()
        }
    } else {
        Box(modifier = Modifier.graphicsLayer(clip = true)) {
            holdContent()
            moveContent()
        }
    }

}

@Composable
fun screenSize(isWidth: Boolean): Dp {
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return if (isWidth) screenWidthDp.dp else screenHeightDp.dp
}



