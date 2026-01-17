package com.wheretogo.presentation.composable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.RouteCategory
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.BottomSheet
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.FocusTextField
import com.wheretogo.presentation.composable.content.KeyboardTrack
import com.wheretogo.presentation.composable.content.NaverMapSheet
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.feature.intervalTab
import com.wheretogo.presentation.feature.naver.placeCurrentLocation
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.theme.Black
import com.wheretogo.presentation.theme.Gray150
import com.wheretogo.presentation.theme.Gray280
import com.wheretogo.presentation.theme.Gray50
import com.wheretogo.presentation.theme.Gray6080
import com.wheretogo.presentation.theme.PrimeBlue
import com.wheretogo.presentation.theme.White
import com.wheretogo.presentation.theme.White85
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily
import com.wheretogo.presentation.toStrRes
import com.wheretogo.presentation.viewmodel.CourseAddViewModel
import kotlinx.coroutines.launch

@Composable
fun CourseAddScreen(
    viewModel: CourseAddViewModel = hiltViewModel()
) {
    val state by viewModel.courseAddScreenState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    CourseAddSheetContent(
        state = state,

        //NaverMap
        onMapAsync = { coroutineScope.launch { it.placeCurrentLocation(context) } },
        onCameraUpdate = { viewModel.handleIntent(CourseAddIntent.CameraUpdated(it)) },
        onMapClick = { viewModel.handleIntent(CourseAddIntent.MapClick(it)) },
        onMarkerClick = { viewModel.handleIntent(CourseAddIntent.WaypointMarkerClick(it)) },

        //SearchBar
        onSearchSubmit = { viewModel.handleIntent(CourseAddIntent.SubmitClick(it)) },
        onSearchBarClick = { viewModel.handleIntent(CourseAddIntent.SearchBarClick) },
        onSearchBarClose = { viewModel.handleIntent(CourseAddIntent.SearchBarClose) },
        onSearchBarItemClick = { viewModel.handleIntent(CourseAddIntent.SearchBarItemClick(it)) },

        //BottomSheet
        onSheetStateChange = { viewModel.handleIntent(CourseAddIntent.SheetStateChange(it)) },

        //CourseAddSheetContent
        onRouteCreateClick = { viewModel.handleIntent(CourseAddIntent.RouteCreateClick) },
        onCategorySelect = { viewModel.handleIntent(CourseAddIntent.RouteCategorySelect(it)) },
        onCommendClick = { viewModel.handleIntent(CourseAddIntent.CommendClick) },
        onBackClick = { viewModel.handleIntent(CourseAddIntent.DetailBackClick) },
        onCourseNameSubmit = { viewModel.handleIntent(CourseAddIntent.CourseNameSubmit(it)) },

        //FloatingButtonGroup
        onMarkerMoveClick = { viewModel.handleIntent(CourseAddIntent.MarkerMoveFloatingClick) },
        onMarkerRemoveClick = { viewModel.handleIntent(CourseAddIntent.MarkerRemoveFloatingClick) }

    )
}

@Composable
fun CourseAddSheetContent(
    state: CourseAddScreenState = CourseAddScreenState(),

    //NaverMap
    onMapAsync: (NaverMap) -> Unit = {},
    onCameraUpdate: (CameraState) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    onMarkerClick: (MarkerInfo) -> Unit = {},

    //SearchBar
    onSearchBarItemClick: (SearchBarItem) -> Unit = {},
    onSearchBarClick: (Boolean) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSearchBarClose: () -> Unit = {},

    //BottomSheet
    onSheetStateChange: (SheetVisibleMode) -> Unit = {},

    //CourseAddSheetContent
    onCategorySelect: (RouteCategory) -> Unit = {},
    onRouteCreateClick: () -> Unit = {},
    onCommendClick: () -> Unit = {},
    onCourseNameSubmit: (String) -> Unit = {},
    onBackClick: () -> Unit = {},

    //FloatingButtonGroup
    onMarkerMoveClick: () -> Unit = {},
    onMarkerRemoveClick: () -> Unit = {}
){
    val isPreview = LocalInspectionMode.current
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
        content = { systemBars->
            var bottomSheetHeight by remember { mutableStateOf(0.dp) }
            val mapBottomPadding by animateDpAsState(
                targetValue = bottomSheetHeight,
                animationSpec = tween(durationMillis = 300)
            )
            val systemBarBottomPadding by remember {
                derivedStateOf { systemBars.calculateBottomPadding() }
            }
            NaverMapSheet(
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxSize(),
                state = state.naverMapState,
                overlayGroup = state.overlayGroup,
                fingerPrint = state.fingerPrint,
                onMapAsync = onMapAsync,
                onCameraUpdate = onCameraUpdate,
                onMapClick = onMapClick,
                onMarkerClick = onMarkerClick,
                contentPadding = ContentPadding(
                    start = systemBars.calculateStartPadding(LocalLayoutDirection.current),
                    end = systemBars.calculateEndPadding(LocalLayoutDirection.current),
                    top = systemBars.calculateTopPadding(),
                    bottom = mapBottomPadding
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = systemBars.calculateStartPadding(LocalLayoutDirection.current),
                        end = systemBars.calculateEndPadding(LocalLayoutDirection.current),
                        top = systemBars.calculateTopPadding()
                    )
            ) {
                SearchBar(
                    modifier = Modifier
                        .zIndex(1f),
                    state = state.searchBarState,
                    onSearchSubmit = onSearchSubmit,
                    onSearchBarClick = onSearchBarClick,
                    onSearchBarClose = onSearchBarClose,
                    onSearchBarItemClick = onSearchBarItemClick
                )

                BottomSheet(
                    modifier = Modifier,
                    bottomSpace = systemBarBottomPadding,
                    isVisible = CourseAddScreenState.isBottomSheetVisible.contains(state.stateMode),
                    onSheetHeightChange = { dp ->
                        bottomSheetHeight = dp
                    },
                    onSheetStateChange = onSheetStateChange,
                    minHeight = state.bottomSheetState.content.minHeight.dp,
                    isSpaceVisibleWhenClose = true
                ) {
                    CourseAddSheetContent(
                        state = state.bottomSheetState.courseAddSheetState,
                        onRouteCreateClick = onRouteCreateClick,
                        onCategorySelect = onCategorySelect,
                        onCommendClick = onCommendClick,
                        onBackClick = onBackClick,
                        onCourseNameSubmit = onCourseNameSubmit,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = systemBars.calculateStartPadding(LocalLayoutDirection.current),
                        end = systemBars.calculateEndPadding(LocalLayoutDirection.current),
                        top = systemBars.calculateTopPadding(),
                        bottom = mapBottomPadding
                    )
            ) {
                if (BuildConfig.TEST_UI) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f)
                    ) {
                        Column(
                            modifier = Modifier.align(alignment = Alignment.TopStart),
                        ) {
                            Text(
                                text = "${state.overlayGroup.size}",
                                fontSize = 50.sp
                            )
                            Text(
                                text = "${state.bottomSheetState.courseAddSheetState.pathType}",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                if (state.isFloatMarker)
                    Box(// 중앙 마커
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .width(30.dp)
                                .padding(bottom = 30.dp),
                            painter = painterResource(R.drawable.ic_marker),
                            contentDescription = ""
                        )
                    }


                if (state.isFloatingButton) {
                    FloatingButtonGroup(
                        modifier = Modifier
                            .align(Alignment.BottomEnd),
                        onMarkerMoveClick = onMarkerMoveClick,
                        onMarkerRemoveClick = onMarkerRemoveClick
                    )
                }
            }
        }
    )

}

@Composable
fun FloatingButtonGroup(
    modifier: Modifier,
    onMarkerMoveClick: () -> Unit,
    onMarkerRemoveClick: () -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(White85)
                    .clickable {
                        onMarkerMoveClick()
                    }, contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(R.drawable.ic_location), "")
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(White85)
                    .clickable {
                        onMarkerRemoveClick()
                    }, contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(R.drawable.ic_close), "")
            }
        }
    }
}

@Composable
fun CourseAddSheetContent(
    state: CourseAddScreenState.CourseAddSheetState,
    onCategorySelect: (RouteCategory) -> Unit,
    onRouteCreateClick: () -> Unit,
    onCommendClick: () -> Unit,
    onCourseNameSubmit: (String) -> Unit = {},
    onBackClick: () -> Unit,
) {
    Column {
        Box(modifier = Modifier.heightIn(min = 340.dp)) {
            SlideAnimation(
                visible = !state.isCategoryStep,
                direction = AnimationDirection.CenterUp
            ) {
                RouteWaypointContent(
                    modifier = Modifier
                        .padding(top = 15.dp, start = 15.dp, end = 15.dp, bottom = 5.dp)
                        .fillMaxWidth(),
                    courseName = state.courseName,
                    duration = state.routeState.duration,
                    waypointItemStateGroup = state.routeState.waypointItemStateGroup,
                    onRouteCreateClick = onRouteCreateClick,
                    onCourseNameSubmit = onCourseNameSubmit,
                )
            }

            SlideAnimation(
                visible = state.isCategoryStep,
                direction = AnimationDirection.CenterDown
            ) {
                RouteDetailContent(
                    modifier = Modifier
                        .padding(15.dp),
                    selectedItemGroup = state.selectedCategoryCodeGroup,
                    onCategorySelect = onCategorySelect,
                    onBackClick = onBackClick
                )
            }
        }
        CommendButton(
            modifier = Modifier.height(60.dp),
            isDetailContent = state.isCategoryStep,
            isDone = if (state.isCategoryStep) state.isTwoStepDone else state.isOneStepDone,
            isLoading = state.isLoading,
            onCommendClick = onCommendClick
        )
    }
}

@Composable
fun CommendButton(
    modifier: Modifier,
    isLoading: Boolean,
    isDone: Boolean,
    isDetailContent: Boolean,
    onCommendClick: () -> Unit
) {
    var isCommendActive by remember { mutableStateOf(isDone) }
    LaunchedEffect(isDone) { isCommendActive = isDone }
    Box(
        modifier = modifier.padding(horizontal = 15.dp, vertical = 5.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val text = if (isDetailContent) R.string.done else R.string.next_step
        val textColor = if (isCommendActive) White else Black
        val backColor = if (isCommendActive) PrimeBlue else White
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    color = Gray6080, shape = RoundedCornerShape(16.dp), width = 1.dp
                )
                .background(backColor)
                .intervalTab(1000L) {
                    if (isCommendActive) onCommendClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if(isLoading)
                DelayLottieAnimation(
                    modifier = Modifier
                        .size(50.dp),
                    ltRes = R.raw.lt_loading,
                    isVisible = true,
                    delay = 0
                )
            else
                Text(
                    text = stringResource(text),
                    color = textColor,
                    fontFamily = interBoldFontFamily
                )
        }
    }
}

@Composable
fun RouteDetailContent(
    modifier: Modifier,
    selectedItemGroup:Map<RouteAttr,Int>,
    onCategorySelect: (RouteCategory) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier.heightIn(max= 500.dp)
    ) {
        Box(
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onBackClick()
            }) {
            Image(
                modifier = Modifier.padding(top = 5.dp, bottom = 15.dp, start = 5.dp, end = 20.dp),
                painter = painterResource(R.drawable.ic_up),
                contentDescription = "",
            )
        }
        Column(
            modifier = Modifier.padding(top = 5.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            RouteCategory(
                type = RouteAttr.TYPE,
                selectedItem = selectedItemGroup.getOrDefault(RouteAttr.TYPE,-1),
                onCategorySelect = onCategorySelect
            )
            RouteCategory(
                type = RouteAttr.LEVEL,
                selectedItem = selectedItemGroup.getOrDefault(RouteAttr.LEVEL,-1),
                onCategorySelect = onCategorySelect
            )
            RouteCategory(
                type = RouteAttr.RELATION,
                selectedItem = selectedItemGroup.getOrDefault(RouteAttr.RELATION,-1),
                onCategorySelect = onCategorySelect
            )
        }

    }
}

@Composable
fun RouteCategory(
    type: RouteAttr,
    selectedItem:Int,
    onCategorySelect: (RouteCategory) -> Unit,
) {
    Column {
        Text(
            modifier = Modifier.padding(start = 5.dp),
            text = stringResource(type.toStrRes()),
            fontSize = 16.sp,
            fontFamily = interBoldFontFamily
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            type.getItems().forEach {
                val isClick = it.code!=-1 && it.code == selectedItem
                RouteDetailItem(it,isClick, onCategorySelect)
            }
        }
    }
}

@Composable
fun RouteDetailItem(
    item: RouteCategory,
    isClick:Boolean,
    onRouteDetailItemClick:(RouteCategory)->Unit,
) {
    val res = item.item.toStrRes()
    val emogi = res.first
    val strRes = res.second
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isClick) PrimeBlue else Gray6080
            )
            .clickable {
                onRouteDetailItemClick(item)
            }) {
        Text(
            modifier = Modifier.padding(vertical = 3.dp, horizontal = 8.dp),
            text = "$emogi ${stringResource(strRes)}",
            color = if (isClick) Color.White else Color.Black,
            fontSize = 11.sp,
            fontFamily = interBoldFontFamily
        )
    }
}

@Composable
fun RouteWaypointContent(
    courseName: String,
    modifier: Modifier,
    duration: Int,
    waypointItemStateGroup: List<CourseAddScreenState.RouteWaypointItemState>,
    onRouteCreateClick: () -> Unit,
    onCourseNameSubmit: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocus by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(courseName) {
        if (courseName.isNotBlank())
            editText = editText.copy(text = courseName)
    }

    Box(modifier = modifier.padding()) {
        Column {
            FadeAnimation(modifier = Modifier, !isFocus) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.padding(start = 5.dp, top = 5.dp), text = "경로",
                        fontSize = 20.sp,
                        fontFamily = interBoldFontFamily
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp, shape = RoundedCornerShape(16.dp), color = Gray150
                            )
                            .align(Alignment.TopEnd)
                            .intervalTab(2000) { onRouteCreateClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            text = "\uD83D\uDCCD 생성",
                            fontSize = 15.sp,
                            fontFamily = interBoldFontFamily
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp, start = 10.dp)
                    .fillMaxWidth()
            ) {
                val textStyle = TextStyle(fontSize = 16.sp, fontFamily = interBoldFontFamily)
                val courseName = stringResource(R.string.course_name)
                SlideAnimation(
                    modifier = Modifier.align(Alignment.TopStart),
                    visible = isFocus,
                    direction = AnimationDirection.CenterDown
                ) {
                    Text(
                        text = courseName,
                        style = textStyle.copy(color = Gray150, fontSize = 11.sp)
                    )
                }
                Column(modifier = Modifier.padding(top = 17.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        var isTextCover by remember { mutableStateOf(true) }

                        Box(
                            modifier = Modifier
                                .heightIn(min = 30.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (isTextCover && editText.text.isBlank())
                                Text(
                                    text = courseName,
                                    style = textStyle.copy(color = Gray150)
                                )
                            FocusTextField(
                                modifier = Modifier,
                                textValue = editText,
                                textStyle = textStyle,
                                readOnly = false,
                                focusRequester = focusRequester,
                                onTextValueChange = {
                                    val isComposing = it.composition != null
                                    editText =
                                        if (isComposing) {
                                            it
                                        } else {
                                            it.copy(selection = TextRange(it.text.length))
                                        }
                                },
                                onTextSubmit = {
                                    isFocus = false
                                    focusManager.clearFocus()
                                    onCourseNameSubmit(it)
                                },
                                onFocusChanged = {
                                    when {
                                        !it.hasFocus -> {
                                            isTextCover = true
                                        }

                                        it.hasFocus -> {
                                            isFocus = true
                                            isTextCover = false
                                        }
                                    }
                                }
                            )
                        }

                        KeyboardTrack(
                            onKeyboardClose = {
                                if (isFocus) {
                                    focusManager.clearFocus()
                                    onCourseNameSubmit(editText.text)
                                    isFocus = false
                                }
                            })
                        Text(
                            "소요시간 : ${duration / 60000}분",
                            fontSize = 15.sp,
                            fontFamily = interFontFamily
                        )
                    }
                }
            }


            Box(modifier.height(160.dp)) {
                FadeAnimation(visible = waypointItemStateGroup.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(waypointItemStateGroup) { item ->
                            AddressItem(
                                modifier = Modifier.animateItem(),
                                item = item
                            )
                        }
                    }
                }
                FadeAnimation(visible = waypointItemStateGroup.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .fillMaxSize()
                            .background(Gray50),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("생성으로 새 경로를 만들어 보세요.", fontFamily = interFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItem(modifier: Modifier, item: CourseAddScreenState.RouteWaypointItemState) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp, color = Gray280, shape = RoundedCornerShape(16.dp)
            )

    ) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.data.alias, fontSize = 12.sp,
                fontFamily = interBoldFontFamily
            )
            Text(
                text = item.data.address, fontSize = 12.sp,
                fontFamily = interFontFamily
            )
        }
    }
}

@Preview(widthDp = 350, heightDp = 600)
@Composable
fun CourseAddOneStepPreview() {
    CourseAddSheetContent(
        state = CourseAddScreenState(
            bottomSheetState = BottomSheetState(
                content = DriveBottomSheetContent.PREVIEW,
                courseAddSheetState = CourseAddScreenState.CourseAddSheetState(
                    isCategoryStep = false,
                    selectedCategoryCodeGroup = mapOf(
                        RouteAttr.TYPE to 1,
                        RouteAttr.LEVEL to 4,
                        RouteAttr.RELATION to 9,
                    )
                )
            )
        )
    )
}

@Preview(widthDp = 350, heightDp = 600)
@Composable
fun CourseAddTwoStepPreview() {
    CourseAddSheetContent(
        state = CourseAddScreenState(
            bottomSheetState = BottomSheetState(
                content = DriveBottomSheetContent.PREVIEW,
                courseAddSheetState = CourseAddScreenState.CourseAddSheetState(
                    isCategoryStep = true,
                    selectedCategoryCodeGroup = mapOf(
                        RouteAttr.TYPE to 1,
                        RouteAttr.LEVEL to 4,
                        RouteAttr.RELATION to 9,
                    )
                )
            ),
        ),
    )
}