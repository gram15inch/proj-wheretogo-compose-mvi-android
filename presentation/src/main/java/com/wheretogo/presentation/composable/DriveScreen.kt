package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.overlay.Marker
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.CheckPointAddContent
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.content.DescriptionTextField
import com.wheretogo.presentation.composable.content.DriveBottomSheet
import com.wheretogo.presentation.composable.content.DriveListContent
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.FloatingButtons
import com.wheretogo.presentation.composable.content.InfoContent
import com.wheretogo.presentation.composable.content.MapPopup
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.naver.setCurrentLocation
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.parse
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(
    navController: NavController,
    viewModel: DriveViewModel = hiltViewModel()
) {
    val state by viewModel.driveScreenState.collectAsState()
    val context = LocalContext.current
    val isWideSize = screenSize(true) > 650.dp
    val coroutineScope = rememberCoroutineScope()
    BackHandler {
        navController.navigateUp()
    }
    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        if(BuildConfig.DEBUG)
            Text(
                modifier = Modifier.align(alignment = Alignment.TopStart),
                text = "${state.mapState.mapOverlayGroup.size}",
                fontSize = 50.sp
            )
        DelayLottieAnimation(
            modifier = Modifier
                .padding(top = 40.dp, end = 10.dp)
                .size(50.dp)
                .align(alignment = Alignment.TopEnd),
            ltRes = R.raw.lt_loading,
            isVisible = state.isLoading,
            delay = 300
        )
    }

    NaverMap(
        modifier = Modifier
            .zIndex(0f)
            .fillMaxSize()
            .navigationBarsPadding(),
        overlayMap = state.mapState.mapOverlayGroup,
        cameraState = state.mapState.cameraState,
        onMapAsync = { map ->
            viewModel.handleIntent(DriveScreenIntent.MapIsReady)
            coroutineScope.launch { map.setCurrentLocation(context) }
        },
        onCameraMove = { camera ->
            viewModel.handleIntent(DriveScreenIntent.UpdateCamera(camera))
        },
        onCourseMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CourseMarkerClick(OverlayTag.parse(marker.tag as String)))
        },
        onCheckPointMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CheckPointMarkerClick(OverlayTag.parse(marker.tag as String)))
        },
        onOverlayRenderComplete = { isRendered ->
            viewModel.handleIntent(DriveScreenIntent.OverlayRenderComplete(isRendered))
        },
        contentPadding = ContentPadding()
    )

    FadeAnimation(visible = state.popUpState.isVisible) {
        BlurEffect(onClick = {
            viewModel.handleIntent(DriveScreenIntent.DismissPopup)
        })
    }


    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        OneHandArea {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, end = 10.dp), contentAlignment = Alignment.CenterEnd
            ) {
                state.searchBarState.run {
                    SlideAnimation (visible = isVisible, direction = AnimationDirection.CenterRight) {
                        SearchBar(
                            isLoading = isLoading,
                            isEmptyVisible = isEmptyVisible,
                            simpleAddressGroup = simpleAddressGroup,
                            onSubmitClick = { viewModel.handleIntent(DriveScreenIntent.SubmitClick(it)) },
                            onSearchToggleClick = { viewModel.handleIntent(DriveScreenIntent.SearchToggleClick(it)) },
                            onAddressItemClick = { viewModel.handleIntent(DriveScreenIntent.AddressItemClick(it)) }
                        )
                    }
                }
            }
            FadeAnimation(
                modifier = Modifier
                    .align(alignment = Alignment.BottomEnd),
                visible = state.listState.isVisible
            ) {
                DriveListContent(
                    modifier = Modifier
                        .align(alignment = Alignment.BottomCenter)
                        .padding(horizontal = 12.dp),
                    listItemGroup = state.listState.listItemGroup,
                    onItemClick = { selectedItem ->
                        viewModel.handleIntent(DriveScreenIntent.DriveListItemClick(selectedItem))
                    },
                    onBookmarkClick = {
                        viewModel.handleIntent(DriveScreenIntent.DriveListItemBookmarkClick(it))
                    },
                    onHeightPxChange = {

                    }
                )
            }

            FadeAnimation(
                modifier = Modifier
                    .align(alignment = Alignment.BottomStart),
                visible = state.popUpState.isVisible
            ) {
                MapPopup(
                    modifier = Modifier.align(Alignment.BottomStart),
                    commentState = state.popUpState.commentState,
                    imageUri = state.popUpState.imageUri,
                    isWideSize = isWideSize,
                    onPopupImageClick = {
                        viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)
                    },
                    onPopupBlurClick = {
                        viewModel.handleIntent(DriveScreenIntent.DismissPopupComment)
                    },
                    onCommentListItemClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(item))
                    },
                    onCommentListItemLongClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentListItemLongClick(item))
                    },
                    onCommentLikeClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentLikeClick(item))
                    },
                    onCommentAddClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentAddClick(item))
                    },
                    onCommentRemoveClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentRemoveClick(item))
                    },
                    onCommentReportClick = { item ->
                        viewModel.handleIntent(DriveScreenIntent.CommentReportClick(item))
                    },
                    onCommentEditValueChange = { textFiled ->
                        viewModel.handleIntent(DriveScreenIntent.CommentEditValueChange(textFiled))
                    },
                    onCommentEmogiPress = { emogi ->
                        viewModel.handleIntent(DriveScreenIntent.CommentEmogiPress(emogi))
                    },
                    onCommentTypePress = { type ->
                        viewModel.handleIntent(DriveScreenIntent.CommentTypePress(type))
                    }
                )
            }

            DriveBottomSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(997f),
                isVisible = state.bottomSheetState.isVisible,
                onBottomSheetClose = {
                    viewModel.handleIntent(DriveScreenIntent.BottomSheetClose)
                }
            ) {
                if (state.bottomSheetState.isCheckPointAdd)
                    CheckPointAddContent(
                        state = state.bottomSheetState.checkPointAddState,
                        onSubmitClick = {
                            viewModel.handleIntent(DriveScreenIntent.CheckpointSubmitClick)
                        },
                        onSliderChange = {
                            viewModel.handleIntent(
                                DriveScreenIntent.CheckpointLocationSliderChange(
                                    it
                                )
                            )
                        },
                        onImageChange = {
                            viewModel.handleIntent(DriveScreenIntent.CheckpointImageChange(it))
                        }
                    )
                else
                    InfoContent(
                        state = state.bottomSheetState.infoState,
                        onRemoveClick = {
                            viewModel.handleIntent(DriveScreenIntent.InfoRemoveClick(it))
                        },
                        onReportClick = {
                            viewModel.handleIntent(DriveScreenIntent.InfoReportClick(it))
                        }
                    )
            }

            val isNotComment = !state.popUpState.commentState.isCommentVisible
            FloatingButtons(
                modifier = Modifier.fillMaxSize(),
                course = state.listState.clickItem.course,
                isCommentVisible = state.floatingButtonState.isCommentVisible && isNotComment,
                isCheckpointAddVisible = state.floatingButtonState.isCheckpointAddVisible && isNotComment,
                isInfoVisible = state.floatingButtonState.isInfoVisible && isNotComment,
                isExportVisible = state.floatingButtonState.isExportVisible && isNotComment,
                isExportBackPlate = state.floatingButtonState.isBackPlateVisible,
                isFoldVisible = state.floatingButtonState.isFoldVisible && isNotComment,
                onCommentClick = {
                    viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)
                },
                onCheckpointAddClick = {
                    viewModel.handleIntent(DriveScreenIntent.CheckpointAddFloatingButtonClick)
                },
                onInfoClick = {
                    viewModel.handleIntent(DriveScreenIntent.InfoFloatingButtonClick)
                },
                onExportMapClick = {
                    viewModel.handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick)
                },
                onFoldClick = {
                    viewModel.handleIntent(DriveScreenIntent.FoldFloatingButtonClick)
                }
            )

            ImeStickyBox(modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .zIndex(999f)) {
                DescriptionTextField(
                    modifier = Modifier.heightIn(min = 60.dp),
                    isVisible = state.bottomSheetState.isVisible && it > 30.dp,
                    focusRequester = state.bottomSheetState.checkPointAddState.focusRequester,
                    text = state.bottomSheetState.checkPointAddState.description,
                    onTextChange = {
                        viewModel.handleIntent(DriveScreenIntent.CheckpointDescriptionChange(it))
                    },
                    onEnterClick = {
                        viewModel.handleIntent(DriveScreenIntent.CheckpointDescriptionEnterClick)
                    }
                )
            }
        }
    }
}


@Composable
fun OneHandArea(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .sizeIn(maxWidth = 650.dp)
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
            .background(color = colorResource(R.color.gray_C7C7C7_80))
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




