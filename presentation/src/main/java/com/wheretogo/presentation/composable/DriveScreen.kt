package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.domain.model.util.ImageInfo
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.WIDE_WIDTH
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.BottomSheet
import com.wheretogo.presentation.composable.content.CheckPointAddContent
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.content.DescriptionTextField
import com.wheretogo.presentation.composable.content.DriveListContent
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.FloatingButtons
import com.wheretogo.presentation.composable.content.GuidePopup
import com.wheretogo.presentation.composable.content.InfoContent
import com.wheretogo.presentation.composable.content.MapPopup
import com.wheretogo.presentation.composable.content.NaverMapSheet
import com.wheretogo.presentation.composable.content.OneHandArea
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.composable.content.ZIndexOfDriveContentArea
import com.wheretogo.presentation.composable.effect.AppEventReceiveEffect
import com.wheretogo.presentation.composable.effect.LifecycleDisposer
import com.wheretogo.presentation.defaultCommentEmogiGroup
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.FloatingButtonState
import com.wheretogo.presentation.state.GuideState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.theme.Gray5060
import com.wheretogo.presentation.theme.Gray6080
import com.wheretogo.presentation.toNavigation
import com.wheretogo.presentation.viewmodel.DriveViewModel

@Composable
fun DriveScreen(
    navController: NavController,
    viewModel: DriveViewModel = hiltViewModel()
) {
    val driveState by viewModel.driveScreenState.collectAsState()

    LifecycleDisposer(
        onEventChange = { viewModel.handleIntent(DriveScreenIntent.LifecycleChange(it)) }
    )

    AppEventReceiveEffect(
        onReceive = {event, bool ->  viewModel.handleIntent(DriveScreenIntent.EventReceive(event,bool)) }
    )

    BackHandler {
        navController.navigateUp()
    }

    viewModel.run {
        DriveContent(
            state = driveState,

            //GuidePopup
            onGuideClick = { handleIntent(DriveScreenIntent.GuidePopupClick(it)) },

            //NaverMap
            onMapAsync = { handleIntent(DriveScreenIntent.MapAsync)},
            onCameraUpdate = { handleIntent(DriveScreenIntent.CameraUpdated(it)) },
            onMarkerClick = { handleIntent(DriveScreenIntent.MarkerClick(it))},

            //Blur
            onBlurClick = { handleIntent(DriveScreenIntent.BlurClick) },

            //Searchbar
            onSearchSubmit = { handleIntent(DriveScreenIntent.SearchSubmit(it)) },
            onSearchBarClick = { handleIntent(DriveScreenIntent.SearchBarClick(it)) },
            onSearchBarItemClick = { handleIntent(DriveScreenIntent.AddressItemClick(it)) },
            onSearchBarClose = { handleIntent(DriveScreenIntent.SearchBarClose) },

            //DriveListContent
            onListItemClick = { handleIntent(DriveScreenIntent.DriveListItemClick(it)) },

            //MapPopup
            onPopupImageClick = {},
            onPopupBlurClick = { handleIntent(DriveScreenIntent.DismissPopupComment) },
            onCommentListItemLongClick = { handleIntent(DriveScreenIntent.CommentListItemLongClick(it)) },
            onCommentListItemClick = { handleIntent(DriveScreenIntent.CommentListItemClick(it)) },
            onCommentLikeClick = { handleIntent(DriveScreenIntent.CommentLikeClick(it)) },
            onCommentAddClick = { handleIntent(DriveScreenIntent.CommentAddClick(it)) },
            onCommentRemoveClick = { handleIntent(DriveScreenIntent.CommentRemoveClick(it)) },
            onCommentReportClick = { handleIntent(DriveScreenIntent.CommentReportClick(it)) },
            onCommentEmogiPress = { handleIntent(DriveScreenIntent.CommentEmogiPress(it)) },
            onCommentTypePress = { handleIntent(DriveScreenIntent.CommentTypePress(it)) },

            //BottomSheet
            onBottomSheetStateChange = { handleIntent(DriveScreenIntent.BottomSheetChange(it)) },

            //CheckPointAddContent
            onCheckPointAddSubmitClick = { handleIntent(DriveScreenIntent.CheckpointSubmitClick) },
            onSliderChange = { handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(it)) },
            onImageChange = { handleIntent(DriveScreenIntent.CheckpointImageChange(it)) },

            //InfoContent
            onInfoRemoveClick = { handleIntent(DriveScreenIntent.InfoRemoveClick) },
            onInfoReportClick = { handleIntent(DriveScreenIntent.InfoReportClick(it)) },

            //FloatingButtons
            onCommentFloatClick = { handleIntent(DriveScreenIntent.CommentFloatingButtonClick) },
            onCheckpointAddFloatClick = { handleIntent(DriveScreenIntent.CheckpointAddFloatingButtonClick) },
            onInfoFloatClick = { handleIntent(DriveScreenIntent.InfoFloatingButtonClick(it)) },
            onExportMapFloatClick = { handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick) },
            onMapAppClick = { handleIntent(DriveScreenIntent.ExportMapAppButtonClick(it)) },
            onFoldFloatClick = { handleIntent(DriveScreenIntent.FoldFloatingButtonClick) },

            //DescriptionTextField
            onTextFieldEnterClick = { handleIntent(DriveScreenIntent.CheckpointDescriptionEnterClick(it)) },
        )
    }

}

@Composable
fun DriveContent(
    state: DriveScreenState = DriveScreenState(),

    //GuidePopup
    onGuideClick: (DriveTutorialStep) -> Unit = {},

    //Navermap
    onMapAsync: () -> Unit = {},
    onCameraUpdate: (CameraState) -> Unit = {},
    onMarkerClick: (MarkerInfo) -> Unit = {},

    //Blur
    onBlurClick: () -> Unit = {},

    //SearchBar
    onSearchBarItemClick: (SearchBarItem) -> Unit = {},
    onSearchBarClick: (Boolean) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSearchBarClose: () -> Unit = {},

    //DriveListContent
    onListItemClick: (ListState.ListItemState) -> Unit = {},

    //MapPopup
    onPopupImageClick: () -> Unit = {},
    onPopupBlurClick: () -> Unit = {},
    onCommentListItemClick: (CommentState.CommentItemState) -> Unit = {},
    onCommentListItemLongClick: (Comment) -> Unit = {},
    onCommentLikeClick: (CommentState.CommentItemState) -> Unit = {},
    onCommentAddClick: (String) -> Unit = {},
    onCommentRemoveClick: (Comment) -> Unit = {},
    onCommentReportClick: (Comment) -> Unit = {},
    onCommentEmogiPress: (String) -> Unit = {},
    onCommentTypePress: (TypeEditText) -> Unit = {},

    //CheckPointAddContent
    onCheckPointAddSubmitClick: () -> Unit = {},
    onSliderChange: (Float) -> Unit = {},
    onImageChange: (ImageInfo) -> Unit = {},

    //InfoContent
    onInfoReportClick: (String) -> Unit = {},
    onInfoRemoveClick: () -> Unit = {},

    //FloatingButtons
    onCommentFloatClick: () -> Unit = {},
    onCheckpointAddFloatClick: () -> Unit = {},
    onInfoFloatClick: (DriveBottomSheetContent) -> Unit = {},
    onExportMapFloatClick: () -> Unit = {},
    onMapAppClick: (Result<Unit>) -> Unit = {},
    onFoldFloatClick: () -> Unit = {},

    //BottomSheet
    onBottomSheetStateChange: (SheetVisibleMode) -> Unit = {},

    //BottomSheetImeStickyBox
    onTextFieldEnterClick: (String) -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    var bottomSheetHeight by remember { mutableStateOf(0.dp) }
    val mapBottomPadding by animateDpAsState(
        targetValue = bottomSheetHeight,
        animationSpec = tween(durationMillis = 300)
    )
    val focusRequester: FocusRequester = remember { FocusRequester() }
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
        content = { systemBars ->
            val systemBarBottomPadding by remember {
                derivedStateOf { systemBars.calculateBottomPadding() }
            }
            NaverMapSheet(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f),
                state = state.naverMapState,
                overlayGroup = state.overlayGroup,
                fingerPrint = state.fingerPrint,
                onMapAsync = { onMapAsync() },
                onCameraUpdate = onCameraUpdate,
                onMarkerClick = onMarkerClick,
                contentPadding = ContentPadding(
                    start = systemBars.calculateStartPadding(LocalLayoutDirection.current),
                    end = systemBars.calculateEndPadding(LocalLayoutDirection.current),
                    top = systemBars.calculateTopPadding(),
                    bottom = maxOf(mapBottomPadding, systemBarBottomPadding)
                )
            )

            FadeAnimation(
                modifier = Modifier
                    .zIndex(1f),
                visible = DriveScreenState.blurVisible.contains(state.stateMode),
                short = FloatingButtonState.backPlateVisible.contains(state.floatingButtonState.stateMode)
            ) {
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

                if (state.isTestUi && !isPreview)
                    Column(modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .padding(5.dp)) {
                        Text(
                            text = "${state.overlayGroup.size}",
                            fontSize = 50.sp
                        )
                        Text(
                            text = "${state.fingerPrint}",
                            fontSize = 16.sp
                        )
                    }

                if(state.isCongrats){
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lt_congrats))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = 1,
                        isPlaying = true,
                        speed = 1.0f
                    )

                    LottieAnimation(
                        modifier = Modifier
                            .zIndex(999f)
                            .fillMaxSize(),
                        composition = composition,
                        progress = { progress }
                    )
                }

                GuidePopup(
                    modifier = Modifier
                        .zIndex(1f)
                        .run {
                        when(state.guideState.alignment){
                            GuideState.Companion.Align.TOP_START -> align(
                                BiasAlignment(
                                    horizontalBias = -0.9f,
                                    verticalBias = -0.8f
                                )
                            )
                            GuideState.Companion.Align.BOTTOM_START -> align(
                                BiasAlignment(
                                    horizontalBias = -0.9f,
                                    verticalBias = 0.3f
                                )
                            )
                        }
                    },
                    state = state.guideState,
                    onClick = onGuideClick
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
                    ZIndexOfDriveContentArea(
                        tutorialStep = state.guideState.tutorialStep,
                        visibleMode = state.stateMode
                    ) { zIdxs, isBlock, isCover->
                        // 블러(선택막기, 커버)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f)
                                .consumptionEvent(isBlock)
                                .run{
                                    if(isCover)
                                        background(Gray5060)
                                    else this
                                }
                        )
                        // 서치바
                        Box(
                            modifier = Modifier
                                .sizeIn(WIDE_WIDTH.dp)
                                .fillMaxWidth()
                                .zIndex(zIdxs.searchBar)
                            , contentAlignment = Alignment.CenterEnd
                        ) {
                            SlideAnimation(
                                visible = DriveScreenState.searchBarVisible.contains(state.stateMode),
                                direction = AnimationDirection.CenterUp
                            ) {
                                SearchBar(
                                    state = state.searchBarState,
                                    onSearchSubmit = onSearchSubmit,
                                    onSearchBarClick = onSearchBarClick,
                                    onSearchBarItemClick = onSearchBarItemClick,
                                    onSearchBarClose = onSearchBarClose
                                )
                            }
                        }

                        // 코스목록
                        SlideAnimation(
                            modifier = Modifier
                                .padding(bottom = systemBarBottomPadding)
                                .align(alignment = Alignment.BottomEnd)
                                .zIndex(zIdxs.driveList),
                            visible = DriveScreenState.itemListVisible.contains(state.stateMode),
                            direction = AnimationDirection.CenterDown,
                        ) {
                            DriveListContent(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .align(alignment = Alignment.BottomCenter),
                                state = state.listState,
                                onItemClick = onListItemClick,
                            )
                        }

                        // 팝업(체크포인트 사진, 댓글)
                        FadeAnimation(
                            modifier = Modifier
                                .padding(bottom = systemBarBottomPadding)
                                .align(alignment = Alignment.BottomStart)
                                .zIndex(zIdxs.mapPopup),
                            visible = DriveScreenState.popUpVisible.contains(state.stateMode)
                        ) {
                            MapPopup(
                                modifier = Modifier.align(Alignment.BottomStart),
                                state = state.popUpState,
                                isLoading = state.isLoading,
                                isImageBlur = zIdxs.mapPopup == 0f,
                                requestCommentOpen = onCommentFloatClick,
                                onPopupBlurClick = onBlurClick,
                                onCommentListItemClick = onCommentListItemClick,
                                onCommentListItemLongClick = onCommentListItemLongClick,
                                onCommentLikeClick = onCommentLikeClick,
                                onCommentAddClick = onCommentAddClick,
                                onCommentRemoveClick = onCommentRemoveClick,
                                onCommentReportClick = onCommentReportClick,
                                onCommentEmogiPress = onCommentEmogiPress,
                                onCommentTypePress = onCommentTypePress,
                                onCommentSheetStateChange = onBottomSheetStateChange
                            )
                        }

                        // 바텀시트(체크포인트 추가, 컨텐츠 정보)
                        BottomSheet(
                            modifier = Modifier
                                .zIndex(zIdxs.bottomSheet),
                            isOpen = DriveScreenState.bottomSheetVisible.contains(state.stateMode),
                            bottomSpace = systemBarBottomPadding,
                            onSheetHeightChange = { bottomSheetHeight = it },
                            onSheetStateChange = onBottomSheetStateChange,
                            minHeight = state.bottomSheetState.content.minHeight.dp,
                            isSpaceVisibleWhenClose = false
                        ) {
                            when (state.bottomSheetState.content) {
                                DriveBottomSheetContent.CHECKPOINT_ADD -> {
                                    CheckPointAddContent(
                                        state = state.bottomSheetState.checkPointAddState,
                                        focusRequester = focusRequester,
                                        onSubmitClick = onCheckPointAddSubmitClick,
                                        onSliderChange = onSliderChange,
                                        onImageChange = onImageChange
                                    )
                                }

                                DriveBottomSheetContent.COURSE_INFO, DriveBottomSheetContent.CHECKPOINT_INFO -> {
                                    InfoContent(
                                        state = state.bottomSheetState.infoState,
                                        onRemoveClick = onInfoRemoveClick,
                                        onReportClick = onInfoReportClick
                                    )
                                }

                                else -> {}
                            }
                        }

                        // 지도 플로팅
                        FloatingButtons(
                            modifier = Modifier
                                .padding(bottom = systemBars.calculateBottomPadding())
                                .fillMaxSize()
                                .zIndex(zIdxs.floatBtn),
                            state = state.floatingButtonState,
                            guideStep = state.guideState.tutorialStep,
                            isVisible = DriveScreenState.floatingVisible.contains(state.stateMode),
                            navigation = state.selectedCourse.toNavigation(),
                            onCommentClick = onCommentFloatClick,
                            onCheckpointAddClick = onCheckpointAddFloatClick,
                            onInfoClick = { onInfoFloatClick(DriveScreenState.infoContent(state.stateMode)) },
                            onExportMapClick = onExportMapFloatClick,
                            onMapAppClick = onMapAppClick,
                            onFoldClick = onFoldFloatClick
                        )
                    }
                }

                // 키보드(체크포인트 추가)
                ImeStickyBoxForBottomSheet(
                    modifier = Modifier
                        .padding(systemBars)
                        .align(alignment = Alignment.BottomCenter),
                    isVisible = DriveScreenState.imeBoxVisible.contains(state.stateMode),
                    focusRequester = focusRequester,
                    onTextFieldEnterClick = onTextFieldEnterClick
                )
            }
        })
}

@Composable
fun BlurEffect(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Gray6080)
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
    )
}

@Composable
fun ImeStickyBoxForBottomSheet(
    modifier: Modifier,
    isVisible: Boolean,
    focusRequester: FocusRequester,
    onTextFieldEnterClick: (String) -> Unit = {}
) {
    ImeStickyBox(modifier = modifier) {
        DescriptionTextField(
            modifier = Modifier.heightIn(min = 60.dp),
            isVisible = isVisible && it > 30.dp,
            focusRequester = focusRequester,
            onEnterClick = onTextFieldEnterClick
        )
    }
}

@Composable
@Preview(name = "explorer", widthDp = 400, heightDp = 600)
fun ExplorerContentPreview() {
    val newListItemGroup = listOf(ListState.ListItemState(course = getCourseDummy()[0]))
    val searchBarItemGroup = listOf(
        SearchBarItem(
            "기흥호수공원 순환",
            "",
        ),
        SearchBarItem(
            "기흥역 ak플라자",
            "경기도 용인시 기흥구 120",
        )
    )
    DriveContent(
        state = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.Explorer,
                listState = listState.copy(
                    listItemGroup = newListItemGroup
                ),
                searchBarState = searchBarState.copy(
                    isActive = true,
                    searchBarItemGroup = searchBarItemGroup
                ),
                guideState = GuideState(
                    tutorialStep = DriveTutorialStep.DRIVE_LIST_ITEM_CLICK
                )
            )
        }
    )
}

@Composable
@Preview(name = "course", widthDp = 400, heightDp = 600)
fun CourseContentPreview() {
    DriveContent(
        state = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail,
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true,
                        commentAddState = CommentAddState(
                            isOneLinePreview = false,
                            emogiGroup = defaultCommentEmogiGroup()
                        )
                    )
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Default
                )
            )
        }
    )
}

@Composable
@Preview(name = "checkpointAdd", widthDp = 400, heightDp = 600)
fun CheckpointAddContentPreview() {
    DriveContent(
        state = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail,
                bottomSheetState = bottomSheetState.copy(
                    infoState = InfoState(isRemoveButton = true),
                    content = DriveBottomSheetContent.CHECKPOINT_ADD,
                    checkPointAddState = CheckPointAddState(
                        isLoading = false,
                        description = "안녕하세요",
                        imgInfo = ImageInfo("", "새로운 사진.jpg", 30L)
                    )
                ),
            )
        }
    )
}

@Composable
@Preview(name = "checkpointImge", widthDp = 400, heightDp = 600)
fun CheckpointImagePreview() {
    DriveContent(
        state = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurCheckpointDetail,
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Popup
                )
            )
        }
    )
}
