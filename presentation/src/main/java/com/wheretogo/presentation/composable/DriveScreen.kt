package com.wheretogo.presentation.composable

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.model.community.ImageInfo
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.WIDE_WIDTH
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.BottomSheet
import com.wheretogo.presentation.composable.content.CheckPointAddContent
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.content.DescriptionTextField
import com.wheretogo.presentation.composable.content.DriveListContent
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.FloatingButtons
import com.wheretogo.presentation.composable.content.InfoContent
import com.wheretogo.presentation.composable.content.LifecycleDisposer
import com.wheretogo.presentation.composable.content.MapPopup
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.composable.content.screenSize
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.naver.setCurrentLocation
import com.wheretogo.presentation.getCommentEmogiGroup
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(
    navController: NavController,
    viewModel: DriveViewModel = hiltViewModel()
) {
    val driveState by viewModel.driveScreenState.collectAsState()

    LifecycleDisposer(
        onEventChange = { viewModel.handleIntent(DriveScreenIntent.LifecycleChange(it)) }
    )

    BackHandler {
        navController.navigateUp()
    }

    viewModel.run {
        DriveContent(
            state = driveState,

            //NaverMap
            onMapAsync = { handleIntent(DriveScreenIntent.MapIsReady) },
            onCameraUpdate = { handleIntent(DriveScreenIntent.CameraUpdated(it)) },
            onCourseMarkerClick = { handleIntent(DriveScreenIntent.CourseMarkerClick(it)) },
            onCheckPointMarkerClick = { handleIntent(DriveScreenIntent.CheckPointMarkerClick(it)) },

            //Blur
            onBlurClick = { handleIntent(DriveScreenIntent.BlurClick) },

            //Searchbar
            onSearchSubmit = { handleIntent(DriveScreenIntent.SearchSubmit(it)) },
            onSearchBarClick = { handleIntent(DriveScreenIntent.SearchBarClick) },
            onSearchBarItemClick = { handleIntent(DriveScreenIntent.AddressItemClick(it)) },
            onSearchBarClose = { handleIntent(DriveScreenIntent.SearchBarClose) },

            //DriveListContent
            onListItemClick = { handleIntent(DriveScreenIntent.DriveListItemClick(it)) },

            //MapPopup
            onPopupImageClick = { handleIntent(DriveScreenIntent.CommentFloatingButtonClick) },
            onPopupBlurClick = { handleIntent(DriveScreenIntent.DismissPopupComment) },
            onCommentListItemClick = { handleIntent(DriveScreenIntent.CommentListItemClick(it)) },
            onCommentListItemLongClick = { handleIntent(DriveScreenIntent.CommentListItemLongClick(it)) },
            onCommentLikeClick = { handleIntent(DriveScreenIntent.CommentLikeClick(it)) },
            onCommentAddClick = { handleIntent(DriveScreenIntent.CommentAddClick(it)) },
            onCommentRemoveClick = { handleIntent(DriveScreenIntent.CommentRemoveClick(it)) },
            onCommentReportClick = { handleIntent(DriveScreenIntent.CommentReportClick(it)) },
            onCommentEditValueChange = { handleIntent(DriveScreenIntent.CommentEditValueChange(it)) },
            onCommentEmogiPress = { handleIntent(DriveScreenIntent.CommentEmogiPress(it)) },
            onCommentTypePress = { handleIntent(DriveScreenIntent.CommentTypePress(it)) },

            //BottomSheet
            onBottomSheetHeightChange = { handleIntent(DriveScreenIntent.ContentPaddingChanged(it.value.toInt())) },
            onBottomSheetStateChange = { handleIntent(DriveScreenIntent.BottomSheetChange(it)) },

            //CheckPointAddContent
            onCheckPointAddSubmitClick = { handleIntent(DriveScreenIntent.CheckpointSubmitClick) },
            onSliderChange = { handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(it)) },
            onImageChange = { handleIntent(DriveScreenIntent.CheckpointImageChange(it)) },

            //InfoContent
            onInfoRemoveClick = { handleIntent(DriveScreenIntent.InfoRemoveClick(it)) },
            onInfoReportClick = { handleIntent(DriveScreenIntent.InfoReportClick(it)) },

            //FloatingButtons
            onCommentFloatClick = { handleIntent(DriveScreenIntent.CommentFloatingButtonClick) },
            onCheckpointAddFloatClick = { handleIntent(DriveScreenIntent.CheckpointAddFloatingButtonClick) },
            onInfoFloatClick = { handleIntent(DriveScreenIntent.InfoFloatingButtonClick) },
            onExportMapFloatClick = { handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick) },
            onMapAppClick = { handleIntent(DriveScreenIntent.ExportMapAppButtonClick(it)) },
            onFoldFloatClick = { handleIntent(DriveScreenIntent.FoldFloatingButtonClick) },

            //DescriptionTextField
            onTextChange = { handleIntent(DriveScreenIntent.CheckpointDescriptionChange(it)) },
            onTextFieldEnterClick = { handleIntent(DriveScreenIntent.CheckpointDescriptionEnterClick) },
        )
    }

}

@Composable
fun DriveContent(
    state: DriveScreenState= DriveScreenState(),

    //Navermap
    onMapAsync: (NaverMap) -> Unit = {},
    onCameraUpdate: (CameraState) -> Unit = {},
    onCourseMarkerClick: (MapOverlay.MarkerContainer) -> Unit = {},
    onCheckPointMarkerClick: (MapOverlay.MarkerContainer) -> Unit = {},

    //Blur
    onBlurClick: () -> Unit = {},

    //SearchBar
    onSearchBarItemClick: (SearchBarItem) -> Unit = {},
    onSearchBarClick: () -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSearchBarClose: ()-> Unit = {},

    //DriveListContent
    onListItemClick: (ListState.ListItemState) -> Unit = {},

    //MapPopup
    onPopupImageClick: () -> Unit = {},
    onPopupBlurClick: () -> Unit = {},
    onCommentListItemClick: (CommentItemState) -> Unit = {},
    onCommentListItemLongClick: (CommentItemState) -> Unit = {},
    onCommentLikeClick: (CommentItemState) -> Unit = {},
    onCommentAddClick: (CommentAddState) -> Unit = {},
    onCommentRemoveClick: (CommentItemState) -> Unit = {},
    onCommentReportClick: (CommentItemState) -> Unit = {},
    onCommentEditValueChange: (TextFieldValue) -> Unit = {},
    onCommentEmogiPress: (String) -> Unit = {},
    onCommentTypePress: (CommentType) -> Unit = {},

    //CheckPointAddContent
    onCheckPointAddSubmitClick: () -> Unit = {},
    onSliderChange: (Float) -> Unit = {},
    onImageChange: (Uri?) ->  Unit = {},

    //InfoContent
    onInfoReportClick: (InfoState) ->  Unit = {},
    onInfoRemoveClick: (InfoState) ->  Unit = {},

    //FloatingButtons
    onCommentFloatClick: () ->  Unit = {},
    onCheckpointAddFloatClick: () ->  Unit = {},
    onInfoFloatClick: () ->  Unit = {},
    onExportMapFloatClick: () ->  Unit = {},
    onMapAppClick: (Result<Unit>) ->  Unit = {},
    onFoldFloatClick: () ->  Unit = {},

    //BottomSheet
    onBottomSheetStateChange: (SheetState) ->  Unit = {},
    onBottomSheetHeightChange: (Dp) ->  Unit = {},

    //BottomSheetImeStickyBox
    onTextChange: (String) ->  Unit = {},
    onTextFieldEnterClick: () -> Unit = {}
){
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    var bottomSheetHeight by remember { mutableStateOf(0.dp) }
    val mapBottomPadding by animateDpAsState(
        targetValue = bottomSheetHeight,
        animationSpec = tween(durationMillis = 300)
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
        content = { systemBars->
            val systemBarBottomPadding by remember {
                derivedStateOf { systemBars.calculateBottomPadding() }
            }
            NaverMap(
                modifier = Modifier
                    .fillMaxSize(),
                state = state.naverMapState,
                overlayGroup = state.overlayGroup,
                onMapAsync = {
                    onMapAsync(it)
                    coroutineScope.launch { it.setCurrentLocation(context) }
                },
                onCameraUpdate = onCameraUpdate,
                onCourseMarkerClick = onCourseMarkerClick,
                onCheckPointMarkerClick = onCheckPointMarkerClick,
                contentPadding = ContentPadding(
                    start = systemBars.calculateStartPadding(LocalLayoutDirection.current),
                    end = systemBars.calculateEndPadding(LocalLayoutDirection.current),
                    top = systemBars.calculateTopPadding(),
                    bottom = maxOf(mapBottomPadding, systemBarBottomPadding)
                )
            )


            FadeAnimation(modifier = Modifier.zIndex(1f),
                visible = state.popUpState.isVisible || state.floatingButtonState.isBackPlateVisible,
                short = state.floatingButtonState.isBackPlateVisible) {
                BlurEffect(onClick = onBlurClick)
            }

            Box(
                modifier = Modifier
                    .zIndex(2f)
                    .padding(
                        top = systemBars.calculateTopPadding(),
                        start = systemBars.calculateStartPadding(LocalLayoutDirection.current),
                        end = systemBars.calculateEndPadding(LocalLayoutDirection.current)
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {

                if(BuildConfig.TEST_UI && !isPreview)
                    Text(
                        modifier = Modifier.align(alignment = Alignment.TopStart),
                        text = "${state.overlayGroup.size}",
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

                OneHandArea {
                    Box(modifier = Modifier
                        .sizeIn(WIDE_WIDTH.dp)
                        .fillMaxWidth(), contentAlignment = Alignment.CenterEnd
                    ) {
                        SlideAnimation(
                            visible = state.searchBarState.isVisible,
                            direction = AnimationDirection.CenterUp
                        ) {
                            SearchBar(
                                modifier = Modifier,
                                state = state.searchBarState,
                                onSearchSubmit = onSearchSubmit,
                                onSearchBarClick = onSearchBarClick,
                                onSearchBarItemClick = onSearchBarItemClick,
                                onSearchBarClose = onSearchBarClose
                            )
                        }
                    }

                    FadeAnimation(
                        modifier = Modifier
                            .padding(bottom = systemBarBottomPadding)
                            .align(alignment = Alignment.BottomEnd),
                        visible = state.listState.isVisible
                    ) {
                        DriveListContent(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .align(alignment = Alignment.BottomCenter),
                            state = state.listState,
                            onItemClick = onListItemClick,
                        )
                    }

                    FadeAnimation(
                        modifier = Modifier
                            .padding(bottom = systemBarBottomPadding)
                            .align(alignment = Alignment.BottomStart),
                        visible = state.popUpState.isVisible
                    ) {
                        MapPopup(
                            modifier = Modifier.align(Alignment.BottomStart),
                            state = state.popUpState,
                            isLoading = state.isLoading,
                            onPopupImageClick = onPopupImageClick,
                            onPopupBlurClick = onPopupBlurClick,
                            onCommentListItemClick = onCommentListItemClick,
                            onCommentListItemLongClick = onCommentListItemLongClick,
                            onCommentLikeClick = onCommentLikeClick,
                            onCommentAddClick = onCommentAddClick,
                            onCommentRemoveClick = onCommentRemoveClick,
                            onCommentReportClick = onCommentReportClick,
                            onCommentEditValueChange = onCommentEditValueChange,
                            onCommentEmogiPress = onCommentEmogiPress,
                            onCommentTypePress = onCommentTypePress
                        )
                    }

                    BottomSheet(
                        modifier = Modifier
                            .zIndex(3f),
                        state = state.bottomSheetState,
                        bottomSpace = systemBarBottomPadding,
                        onSheetHeightChange = {
                            if (state.bottomSheetState.content != DriveBottomSheetContent.INFO) {
                                bottomSheetHeight = it
                                onBottomSheetHeightChange(it)
                            }

                        },
                        onSheetStateChange = onBottomSheetStateChange
                    ) {
                        when (state.bottomSheetState.content) {
                            DriveBottomSheetContent.CHECKPOINT_ADD -> {
                                CheckPointAddContent(
                                    state = state.bottomSheetState.checkPointAddState,
                                    onSubmitClick = onCheckPointAddSubmitClick,
                                    onSliderChange = onSliderChange,
                                    onImageChange = onImageChange
                                )
                            }

                            DriveBottomSheetContent.INFO -> {
                                InfoContent(
                                    state = state.bottomSheetState.infoState,
                                    onRemoveClick = onInfoRemoveClick,
                                    onReportClick = onInfoReportClick
                                )
                            }

                            else -> {}
                        }
                    }
                    val isNotOtherVisible = !state.popUpState.commentState.isCommentVisible && !state.bottomSheetState.isVisible
                    FloatingButtons(
                        modifier = Modifier
                            .padding(bottom = systemBars.calculateBottomPadding())
                            .fillMaxSize(),
                        state = state.floatingButtonState,
                        isNotOtherVisible = isNotOtherVisible,
                        course = state.listState.clickItem.course,
                        onCommentClick = onCommentFloatClick,
                        onCheckpointAddClick = onCheckpointAddFloatClick,
                        onInfoClick = onInfoFloatClick,
                        onExportMapClick = onExportMapFloatClick,
                        onMapAppClick = onMapAppClick,
                        onFoldClick = onFoldFloatClick
                    )
                }

                BottomSheetImeStickyBox(
                    modifier = Modifier
                        .padding(systemBars)
                        .align(alignment = Alignment.BottomCenter),
                    state = state.bottomSheetState,
                    onTextChange = onTextChange,
                    onTextFieldEnterClick = onTextFieldEnterClick
                )
            }
        })
}

@Composable
fun OneHandArea(content: @Composable BoxScope.() -> Unit) {
    val min = minOf(screenSize(true), 800.dp)
    Box(
        modifier = Modifier
            .sizeIn(maxWidth = min)
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
        Row {
            holdContent()
            moveContent()
        }
    } else {
        Box(modifier = Modifier.graphicsLayer(clip = true), contentAlignment = Alignment.BottomCenter) {
            holdContent()
            moveContent()
        }
    }

}

@Composable
fun BottomSheetImeStickyBox(
    modifier: Modifier, state: BottomSheetState,
    onTextChange: (String) -> Unit = {},
    onTextFieldEnterClick: () -> Unit = {}
) {
    ImeStickyBox(modifier = modifier){
        DescriptionTextField(
            modifier = Modifier.heightIn(min = 60.dp),
            isVisible = state.isVisible && it > 30.dp,
            focusRequester = state.checkPointAddState.focusRequester,
            text = state.checkPointAddState.description,
            onTextChange = onTextChange,
            onEnterClick = onTextFieldEnterClick
        )
    }
}

@Composable
@Preview(name="default",widthDp = 400, heightDp = 600)
fun DefaultContentPreview(){
    val newListItemGroup = listOf(ListState.ListItemState(course = getCourseDummy()[0]))
    val searchBarItemGroup = listOf(SearchBarItem(
        "기흥호수공원 순환",
        "",
    ),
        SearchBarItem(
            "기흥역 ak플라자",
            "경기도 용인시 기흥구 120",
        ))
    DriveContent(
        state = DriveScreenState().run {
            copy(
                listState= listState.copy(
                    isVisible = true,
                    listItemGroup = newListItemGroup
                ),
                searchBarState = searchBarState.copy(
                    isActive = true,
                    searchBarItemGroup = searchBarItemGroup
                ),
            )
        }
    )
}

@Composable
@Preview(name="checkpointAdd",widthDp = 400, heightDp = 600)
fun CheckpointAddContentPreview(){
    DriveContent(
        state = DriveScreenState().run {
            copy(
                searchBarState = searchBarState.copy(
                    isVisible = false
                ),
                bottomSheetState = bottomSheetState.copy(
                    initHeight = 400,
                    infoState = InfoState(isRemoveButton = true),
                    content = DriveBottomSheetContent.CHECKPOINT_ADD,
                    checkPointAddState = CheckPointAddState(
                        isLoading = false,
                        description = "안녕하세요",
                        imgInfo = ImageInfo("".toUri(), "새로운 사진.jpg", 30L)
                    )
                )
            )
        }
    )
}

@Composable
@Preview(name="comment",widthDp = 400, heightDp = 600)
fun CommentContentPreview(){
    DriveContent(
        state = DriveScreenState().run {
            copy(
                searchBarState = searchBarState.copy(
                    isVisible = false
                ),
                popUpState = popUpState.copy(
                    isVisible = true,
                    commentState = popUpState.commentState.copy(
                        isCommentVisible = true,
                        commentAddState = CommentAddState(
                            isEmogiGroup = true,
                            emogiGroup = getCommentEmogiGroup()
                        )
                    )
                ),
                floatingButtonState = floatingButtonState.copy(
                    isCheckpointAddVisible = true,
                    isInfoVisible = true,
                    isExportVisible = true,
                    isFoldVisible = true,
                )
            )
        }
    )
}

@Composable
@Preview(name="checkpoint",widthDp = 400, heightDp = 600)
fun CheckpointContentPreview(){
    DriveContent(
        state = DriveScreenState().run {
            copy(
                searchBarState = searchBarState.copy(
                    isVisible = false
                ),
                popUpState = popUpState.copy(
                    isVisible = true
                ),
                floatingButtonState = floatingButtonState.copy(
                    isCommentVisible = true,
                    isInfoVisible = true,
                    isExportVisible = true,
                    isFoldVisible = true,
                )
            )
        }
    )
}
