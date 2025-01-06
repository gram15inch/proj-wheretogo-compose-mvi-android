package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.parseMarkerTag
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.CheckpointAddBottomSheet
import com.wheretogo.presentation.composable.content.DescriptionTextField
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.FloatingButtons
import com.wheretogo.presentation.composable.content.MapPopup
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.naver.setCurrentLocation
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(
    navController: NavController,
    viewModel: DriveViewModel = hiltViewModel()
) {
    val state by viewModel.driveScreenState.collectAsState()
    val isWideSize = screenSize(true) > 650.dp
    val context = LocalContext.current
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
        Text(
            modifier = Modifier.align(alignment = Alignment.TopStart),
            text = "${state.mapState.mapOverlayGroup.size}",
            fontSize = 50.sp
        )
        DelayLottieAnimation(
            modifier = Modifier
                .size(50.dp)
                .align(alignment = Alignment.TopEnd),
            isVisible = state.isLoading,
            delay = 300
        )

    }

    NaverMap(
        modifier = Modifier
            .zIndex(0f)
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
        overlayMap = state.mapState.mapOverlayGroup,
        onMapAsync = { map ->
            viewModel.handleIntent(DriveScreenIntent.MapIsReady)
            coroutineScope.launch { map.setCurrentLocation(context) }
        },
        onCameraMove = { camera ->
            viewModel.handleIntent(DriveScreenIntent.UpdateCamera(camera))
        },
        onCourseMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CourseMarkerClick(parseMarkerTag(marker.tag as String)))
        },
        onCheckPointMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CheckPointMarkerClick(parseMarkerTag(marker.tag as String)))
        },
        onOverlayRenderComplete = { isRendered ->
            viewModel.handleIntent(DriveScreenIntent.OverlayRenderComplete(isRendered))
        },
        contentPadding = ContentPadding(
            bottom = 100.dp
        )
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
            FadeAnimation(
                modifier = Modifier
                    .align(alignment = Alignment.BottomEnd),
                visible = state.listState.isVisible
            ) {
                DriveList(
                    modifier = Modifier
                        .align(alignment = Alignment.BottomCenter)
                        .padding(horizontal = 12.dp),
                    listItemGroup = state.listState.listItemGroup,
                    onItemClick = { selectedItem ->
                        viewModel.handleIntent(DriveScreenIntent.DriveListItemClick(selectedItem))
                    },
                    onBookmarkClick = {
                        viewModel.handleIntent(DriveScreenIntent.DriveListItemBookmarkClick(it))
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

            CheckpointAddBottomSheet(
                modifier = Modifier.align(Alignment.BottomCenter),
                state = state.bottomSheetState,
                onSubmitClick = {
                    viewModel.handleIntent(DriveScreenIntent.CheckpointSubmitClick)
                },
                onBottomSheetClose = {
                    viewModel.handleIntent(DriveScreenIntent.BottomSheetClose)
                },
                onSliderChange = {
                    viewModel.handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(it))
                },
                onImageChange = {
                    viewModel.handleIntent(DriveScreenIntent.CheckpointImageChange(it))
                }
            )

            val isNotComment = !state.popUpState.commentState.isCommentVisible
            FloatingButtons(
                modifier = Modifier.fillMaxSize(),
                course = state.listState.clickItem.course,
                isCommentVisible = state.floatingButtonState.isCommentVisible && isNotComment,
                isCheckpointAddVisible = state.floatingButtonState.isCheckpointAddVisible && isNotComment,
                isExportVisible = state.floatingButtonState.isExportVisible && isNotComment,
                isExportBackPlate = state.floatingButtonState.isBackPlateVisible,
                isFoldVisible = state.floatingButtonState.isFoldVisible && isNotComment,
                onCommentClick = {
                    viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)
                },
                onCheckpointAddClick = {
                    viewModel.handleIntent(DriveScreenIntent.CheckpointAddFloatingButtonClick)
                },
                onExportMapClick = {
                    viewModel.handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick)
                },
                onFoldClick = {
                    viewModel.handleIntent(DriveScreenIntent.FoldFloatingButtonClick)
                }
            )

            ImeStickyBox(modifier = Modifier.align(alignment = Alignment.BottomCenter)) {
                DescriptionTextField(
                    modifier = Modifier.heightIn(min = 60.dp),
                    isVisible = state.bottomSheetState.isVisible && it > 30.dp,
                    focusRequester = state.bottomSheetState.focusRequester,
                    text = state.bottomSheetState.description,
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

@Composable
fun DelayLottieAnimation(modifier: Modifier, isVisible: Boolean, delay: Long) {
    var shouldShowAnimation by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<Job?>(null) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lt_loading))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        clipSpec = LottieClipSpec.Progress(0f, 0.4f),
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            if (animation == null)
                animation = launch {
                    delay(delay)
                    shouldShowAnimation = true
                }
        } else {
            animation?.cancel()
            animation = null
            shouldShowAnimation = false
        }
    }
    if (shouldShowAnimation)
        LottieAnimation(
            modifier = modifier,
            composition = composition,
            progress = { progress },
        )
}


