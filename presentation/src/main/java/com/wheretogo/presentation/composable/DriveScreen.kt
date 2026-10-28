package com.wheretogo.presentation.composable
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dhkim139.core.ui.event.AppEventReceiveEffect
import com.dhkim139.core.ui.model.ContentPadding
import com.dhkim139.core.ui.screen.LifecycleDisposer
import com.dhkim139.core.ui.screen.consumptionEvent
import com.dhkim139.core.ui.theme.Palette
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.R
import com.wheretogo.presentation.WIDE_WIDTH
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.BottomSheet
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.content.DriveListContent
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.FloatingButtons
import com.wheretogo.presentation.composable.content.GuidePopup
import com.wheretogo.presentation.composable.content.InfoContent
import com.wheretogo.presentation.composable.content.MapAttribution
import com.wheretogo.presentation.composable.content.MapPopup
import com.wheretogo.presentation.composable.content.NavOptionBottomSheet
import com.wheretogo.presentation.composable.content.NaverMapSheet
import com.wheretogo.presentation.composable.content.OneHandArea
import com.wheretogo.presentation.composable.content.OneTimeLottieAnimation
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.composable.content.ZIndexOfDriveContentArea
import com.wheretogo.presentation.composable.content.rememberMapViewWithLifecycle
import com.wheretogo.presentation.defaultCommentEmogiGroup
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.intent.MapIntent
import com.wheretogo.presentation.model.NaverMapStyle
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.GuideState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.viewmodel.DriveViewModel
import com.wheretogo.presentation.viewmodel.MapViewModel

@Composable
fun DriveScreen(
    driveViewModel: DriveViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
    onBackPressed: ()->Unit
) {
    val driveState by driveViewModel.driveScreenState.collectAsState()
    LifecycleDisposer(
        onEventChange = { driveViewModel.handleIntent(DriveScreenIntent.LifecycleChange(it)) }
    )

    AppEventReceiveEffect(
        onReceive = {driveViewModel.handleIntent(DriveScreenIntent.EventReceive(it)) }
    )

    BackHandler {
        when(driveViewModel.driveScreenState.value.stateMode){
            DriveVisibleMode.Explorer->{ onBackPressed() }
            else -> driveViewModel.handleIntent(DriveScreenIntent.FoldFloatingButtonClick)
        }
    }

    LaunchedEffect(Unit) {
        driveViewModel.driveEvent.collect {
            when(it){
                is DriveEvent.MoveCamera ->  mapViewModel.handleIntent(MapIntent.MoveCamera(it.option))
                is DriveEvent.Focus ->  mapViewModel.handleIntent(MapIntent.Focus(it.item))
                is DriveEvent.Release ->  mapViewModel.handleIntent(MapIntent.RELEASE)
                is DriveEvent.RefreshContent ->  mapViewModel.handleIntent(MapIntent.RefreshContent(it.option))
                is DriveEvent.RefreshOverlay ->  mapViewModel.handleIntent(MapIntent.RefreshOverlay(it.option))
                is DriveEvent.ClearMap ->  mapViewModel.handleIntent(MapIntent.ClearMap)
            }
        }
    }

    LifecycleDisposer {
        mapViewModel.handleIntent(MapIntent.LifeCycleChange(it))
    }

    DriveContent(
        state = driveState,
        mapViewModel = mapViewModel,
        driveViewModel = driveViewModel,
        onIntent = { driveViewModel.handleIntent(it) }
    )
}

@Composable
fun DriveContent(
    state: DriveScreenState = DriveScreenState(),
    driveViewModel: DriveViewModel? = null,
    mapViewModel: MapViewModel? = null,
    onIntent: (DriveScreenIntent) -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    var bottomSheetPadding by remember { mutableStateOf(0.dp) }
    var endSheetPadding by remember { mutableStateOf(0.dp) }

    fun onBlurClick() = onIntent(DriveScreenIntent.BlurClick)
    fun onCommentFloatClick() = onIntent(DriveScreenIntent.CommentFloatingButtonClick)
    fun onFoldFloatClick() = onIntent(DriveScreenIntent.FoldFloatingButtonClick)

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
        content = { systemBars ->
            val systemBarBottomPadding by remember {
                derivedStateOf { systemBars.calculateBottomPadding() }
            }

            if(mapViewModel!= null) {
                val fingerPrint by mapViewModel.fingerPrint.collectAsState()
                val mapState by mapViewModel.state.collectAsState()
                Box(modifier = Modifier.fillMaxSize()){
                    NaverMapSheet(
                        mapView = rememberMapViewWithLifecycle(),
                        state = mapState.naverMapState,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f),
                        style = NaverMapStyle.Basic.copy(zoomControlEnabled = state.isTestUi),
                        overlayGroup = mapViewModel.overlays,
                        fingerPrint = fingerPrint,
                        event = mapViewModel.event,
                        contentPadding = ContentPadding(
                            start = systemBars.calculateStartPadding(LocalLayoutDirection.current),
                            end = maxOf(endSheetPadding, systemBars.calculateEndPadding(LocalLayoutDirection.current)),
                            top = systemBars.calculateTopPadding(),
                            bottom = maxOf(bottomSheetPadding, systemBarBottomPadding)
                        ),
                        onMapAsync = { mapViewModel.handleIntent(MapIntent.MapAsync) },
                        onMarkerClick = { mapViewModel.handleIntent(MapIntent.MarkerClick(it)) },
                        onCameraUpdate = { mapViewModel.handleIntent(MapIntent.CameraUpdated(it)) },
                    )

                    DelayLottieAnimation(
                        modifier = Modifier
                            .padding(bottom = 200.dp, end = 10.dp)
                            .size(130.dp)
                            .align(alignment = Alignment.Center),
                        ltRes = R.raw.lt_star_search,
                        isVisible = mapState.isOverlayLoading,
                        delay = 300,
                        max = 1f
                    )

                    // 디버그
                    if (state.isTestUi && !isPreview)
                        Column(modifier = Modifier
                            .systemBarsPadding()
                            .clickable {
                                onIntent(DriveScreenIntent.DebugOverlayClick)
                            }
                            .align(alignment = Alignment.TopStart)
                            .padding(5.dp)) {
                            Text(
                                text = "${mapViewModel.overlays.size}",
                                fontSize = 50.sp
                            )
                            Text(
                                text = "$fingerPrint",
                                fontSize = 16.sp
                            )
                            mapState.naverMapState .apply {
                                Text(
                                    text = "${(latestCameraState.zoom *10).toInt()/10.0}",
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "${(ZOOM.getZoomCategory(latestCameraState.zoom))}",
                                    fontSize = 20.sp
                                )
                            }

                        }
                }
            }

            FadeAnimation(
                modifier = Modifier
                    .zIndex(1f),
                visible = DriveScreenState.blurVisible.contains(state.stateMode),
            ) {
                BlurEffect(onClick = ::onBlurClick)
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

                if(state.isCongrats){
                    OneTimeLottieAnimation(
                        modifier = Modifier
                            .zIndex(999f)
                            .fillMaxSize(),
                        ltRes = R.raw.lt_congrats
                    )
                }

                GuidePopup(
                    modifier = Modifier
                        .zIndex(2f)
                        .run {
                            when (state.guideState.alignment) {
                                GuideState.Companion.Align.TOP_START -> align(
                                    BiasAlignment(
                                        horizontalBias = -0.9f,
                                        verticalBias = -0.8f
                                    )
                                )
                                GuideState.Companion.Align.CENTER_START -> align(
                                    BiasAlignment(
                                        horizontalBias = -0.9f,
                                        verticalBias = 0.05f
                                    )
                                )
                            }
                        },
                    state = state.guideState,
                    onClick = { onIntent(DriveScreenIntent.GuidePopupClick(it)) }
                )

                OneHandArea(Modifier.zIndex(1f)) {
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
                                        alpha(0.4f)
                                            .background(Palette.Gray50)
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
                                    onSearchSubmit = { onIntent(DriveScreenIntent.SearchSubmit(it)) },
                                    onSearchBarClick = { onIntent(DriveScreenIntent.SearchBarClick(it)) },
                                    onSearchBarItemClick = { onIntent(DriveScreenIntent.AddressItemClick(it)) },
                                    onSearchBarClose = { onIntent(DriveScreenIntent.SearchBarClose) }
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
                                    .padding(bottom = 10.dp)
                                    .align(alignment = Alignment.BottomCenter),
                                state = state.listState,
                                onItemClick = { onIntent(DriveScreenIntent.DriveListItemClick(it)) },
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
                                requestCommentOpen = ::onCommentFloatClick,
                                onPopupBlurClick = ::onBlurClick,
                                onPopupSlide = { onIntent(DriveScreenIntent.PopupImageSlide(it)) },
                                onCommentListItemClick = { onIntent(DriveScreenIntent.CommentListItemClick(it)) },
                                onCommentListItemLongClick = { onIntent(DriveScreenIntent.CommentListItemLongClick(it)) },
                                onCommentAddClick = { onIntent(DriveScreenIntent.CommentAddClick(it)) },
                                onCommentLikeClick = { onIntent(DriveScreenIntent.CommentLikeClick(it)) },
                                onCommentRemoveClick = { onIntent(DriveScreenIntent.CommentRemoveClick(it)) },
                                onCommentReportClick = { c, r -> onIntent(DriveScreenIntent.CommentReportClick(c,r)) },
                                onCommentEmogiPress = { onIntent(DriveScreenIntent.CommentEmogiPress(it)) },
                                onCommentTypePress = { onIntent(DriveScreenIntent.CommentTypePress(it)) },
                                onCommentSheetStateChange = { onIntent(DriveScreenIntent.BottomSheetChange(it)) },
                                onBackPressed = ::onBlurClick
                            )
                        }

                        // 바텀시트(컨텐츠 정보)
                        BottomSheet(
                            modifier = Modifier
                                .zIndex(zIdxs.bottomSheet),
                            isOpen = DriveScreenState.bottomSheetVisible.contains(state.stateMode),
                            bottomSpace = systemBarBottomPadding,
                            onSheetHeightChange = { bottomSheetPadding = it },
                            onSheetStateChange = { onIntent(DriveScreenIntent.BottomSheetChange(it)) },
                            minHeight = state.bottomSheetState.content.minHeight.dp,
                            isSpaceVisibleWhenClose = false
                        ) {
                            when (state.bottomSheetState.content) {
                                DriveBottomSheetContent.COURSE_INFO,
                                DriveBottomSheetContent.CHECKPOINT_INFO -> {
                                    InfoContent(
                                        state = state.bottomSheetState.infoState,
                                        onRemoveClick = { onIntent(DriveScreenIntent.InfoRemoveClick) },
                                        onReportClick = { onIntent(DriveScreenIntent.InfoReportClick(it)) }
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
                            onCommentClick = ::onCommentFloatClick,
                            onInfoClick = { onIntent(DriveScreenIntent.InfoFloatingButtonClick(DriveScreenState.infoContent(state.stateMode))) },
                            onExportMapClick = { onIntent(DriveScreenIntent.ExportMapFloatingButtonClick) },
                            onFoldClick = ::onFoldFloatClick,
                        )
                    }
                }
                Box(
                    Modifier
                        .zIndex(0f)
                        .systemBarsPadding()
                        .padding(horizontal = 2.dp)
                ){
                    MapAttribution(Modifier)
                }
            }
        })

    if(driveViewModel!= null && mapViewModel!=null)
        NavOptionBottomSheet(
            driveViewModel = driveViewModel,
            mapViewModel = mapViewModel,
            onPaddingUpdate = { bottom, end->
                bottomSheetPadding = bottom
                endSheetPadding = end
            },
            step = state.guideState.tutorialStep,
        )
}

@Composable
fun BlurEffect(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(0.5f)
            .background(Palette.Gray100)
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
    )
}

@Composable
@Preview(name = "explorer", widthDp = 400, heightDp = 600)
fun ExplorerContentPreview() {
    val newListItemGroup = listOf(ListState.ListItemState(courseRenderItem = Course.dummy.toDirectionItem()))
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
                guideState = GuideState(
                    tutorialStep = DriveTutorialStep.DRIVE_LIST_ITEM_CLICK
                ),
                searchBarState = searchBarState.copy(
                    isActive = true,
                    searchBarItemGroup = searchBarItemGroup
                ),
                listState = listState.copy(
                    listItemGroup = newListItemGroup
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
                ),
                stateMode = DriveVisibleMode.CourseDetail
            )
        }
    )
}