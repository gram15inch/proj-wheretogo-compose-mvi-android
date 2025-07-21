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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.RouteCategory

import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.BottomSheet
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.feature.intervalTab
import com.wheretogo.presentation.feature.naver.setCurrentLocation
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily
import com.wheretogo.presentation.toStrRes
import com.wheretogo.presentation.viewmodel.CourseAddViewModel
import kotlinx.coroutines.launch
import kotlin.math.max

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
        onMapAsync = { coroutineScope.launch { it.setCurrentLocation(context) } },
        onCameraUpdate = { viewModel.handleIntent(CourseAddIntent.CameraUpdated(it)) },
        onMapClick = { viewModel.handleIntent(CourseAddIntent.MapClick(it)) },
        onCheckPointMarkerClick = { viewModel.handleIntent(CourseAddIntent.WaypointMarkerClick(it)) },

        //SearchBar
        onSearchSubmit = { viewModel.handleIntent(CourseAddIntent.SubmitClick(it)) },
        onSearchBarClick = { viewModel.handleIntent(CourseAddIntent.SearchBarClick) },
        onSearchBarClose = { viewModel.handleIntent(CourseAddIntent.SearchBarClose) },
        onSearchBarItemClick = { viewModel.handleIntent(CourseAddIntent.SearchBarItemClick(it)) },

        //BottomSheet
        onHeightChange = { viewModel.handleIntent(CourseAddIntent.ContentPaddingChanged(it.value.toInt())) },
        onSheetStateChange = { viewModel.handleIntent(CourseAddIntent.SheetStateChange(it)) },

        //CourseAddSheetContent
        onRouteCreateClick = { viewModel.handleIntent(CourseAddIntent.RouteCreateClick) },
        onCategorySelect = { viewModel.handleIntent(CourseAddIntent.RouteCategorySelect(it)) },
        onCommendClick = { viewModel.handleIntent(CourseAddIntent.CommendClick) },
        onBackClick = { viewModel.handleIntent(CourseAddIntent.DetailBackClick) },
        onNameEditValueChange = { viewModel.handleIntent(CourseAddIntent.NameEditValueChange(it)) },

        //FloatingButtonGroup
        onMarkerMoveClick = { viewModel.handleIntent(CourseAddIntent.MarkerMoveFloatingClick) },
        onMarkerRemoveClick = { viewModel.handleIntent(CourseAddIntent.MarkerRemoveFloatingClick) }

    )
}

@Composable
fun CourseAddSheetContent(
    state:CourseAddScreenState = CourseAddScreenState(),

    //NaverMap
    onMapAsync: (NaverMap) -> Unit = {},
    onCameraUpdate: (CameraState) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    onCheckPointMarkerClick: (MapOverlay.MarkerContainer) -> Unit = {},

    //SearchBar
    onSearchBarItemClick: (SearchBarItem) -> Unit = {},
    onSearchBarClick: () -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSearchBarClose: ()-> Unit = {},

    //BottomSheet
    onSheetStateChange: (SheetState) -> Unit = {},
    onHeightChange: (Dp) -> Unit = {},

    //CourseAddSheetContent
    onCategorySelect: (RouteCategory) -> Unit = {},
    onRouteCreateClick: () -> Unit = {},
    onCommendClick: () -> Unit = {},
    onNameEditValueChange: (String) -> Unit = {},
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
            NaverMap(
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxSize(),
                state = state.naverMapState,
                overlayGroup = state.overlayGroup,
                onMapAsync = onMapAsync,
                onCameraUpdate = onCameraUpdate,
                onMapClick = onMapClick,
                onCheckPointMarkerClick = onCheckPointMarkerClick,
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
                    state = state.bottomSheetState,
                    bottomSpace = systemBarBottomPadding,
                    onSheetHeightChange = { dp ->
                        bottomSheetHeight = dp
                        onHeightChange(bottomSheetHeight)
                    },
                    onSheetStateChange = onSheetStateChange
                ) {
                    CourseAddSheetContent(
                        state = state.bottomSheetState.courseAddSheetState,
                        onRouteCreateClick = onRouteCreateClick,
                        onCategorySelect = onCategorySelect,
                        onCommendClick = onCommendClick,
                        onBackClick = onBackClick,
                        onNameEditValueChange = onNameEditValueChange,
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
            ){
                if (BuildConfig.TEST_UI) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f)
                    ) {
                        Text(
                            modifier = Modifier.align(alignment = Alignment.TopStart),
                            text = "${state.overlayGroup.size}",
                            fontSize = 50.sp
                        )
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
                    .background(color = colorResource(R.color.white_85))
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
                    .background(color = colorResource(R.color.white_85))
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
    onNameEditValueChange: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Column {
        Box(modifier = Modifier.heightIn(min=320.dp)) {
            SlideAnimation(
                visible = !state.isTwoStep,
                direction = AnimationDirection.CenterUp
            ) {
                RouteWaypointContent(
                    modifier = Modifier
                        .padding(top = 15.dp, start = 15.dp, end = 15.dp, bottom = 5.dp)
                        .fillMaxWidth(),
                    routeName = state.courseName,
                    duration = state.routeState.duration,
                    waypointItemStateGroup = state.routeState.waypointItemStateGroup,
                    onRouteCreateClick = onRouteCreateClick,
                    onNameEditValueChange = onNameEditValueChange,
                )
            }

            SlideAnimation(
                visible = state.isTwoStep,
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
            isDetailContent = state.isTwoStep,
            isDone = state.isNextStepButtonActive,
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
    LaunchedEffect(isDone) { isCommendActive=isDone }
    Box(
        modifier = modifier.padding(horizontal = 15.dp, vertical = 5.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val text = if (isDetailContent) R.string.done else R.string.next_step
        val textColor = if (isCommendActive) R.color.white else R.color.black
        val backColor = if (isCommendActive) R.color.blue else R.color.white
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    color = colorResource(R.color.gray_C7C7C7_80),
                    shape = RoundedCornerShape(16.dp),
                    width = 1.dp
                )
                .background(colorResource(backColor))
                .intervalTab(1000L) {
                    if(isCommendActive)
                        onCommendClick()
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
                    color = colorResource(textColor),
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
                colorResource(if (isClick) R.color.blue else R.color.gray_C7C7C7_80)
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
    modifier: Modifier,
    routeName: String,
    duration: Int,
    waypointItemStateGroup: List<CourseAddScreenState.RouteWaypointItemState>,
    onRouteCreateClick: () -> Unit,
    onNameEditValueChange: (String) -> Unit,
) {
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val density= LocalDensity.current
    var isFocus by remember { mutableStateOf(false) }
    val imePadding = if(isFocus) with(density) {
        (max(imeBottom - 100, 0)*0.2f).toDp()
    } else 0.dp

    Box(modifier = modifier.padding(bottom = imePadding)) {
        Column {
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
                            width = 1.dp,
                            shape = RoundedCornerShape(16.dp),
                            color = colorResource(R.color.gray_B9B9B9)
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
            Column (
                modifier = Modifier
                    .padding(top = 15.dp, bottom = 10.dp, start = 10.dp)
                    .fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val textStyle = TextStyle(fontSize = 16.sp, fontFamily = interBoldFontFamily)
                    val focusManager = LocalFocusManager.current

                    BasicTextField(
                        modifier = Modifier
                            .onFocusChanged {
                                isFocus = it.isFocused
                            }
                            .fillMaxWidth(),
                        value = routeName,
                        onValueChange = onNameEditValueChange,
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (routeName.isEmpty()) {
                                    Text(
                                        text = "경로 이름",
                                        style = textStyle.copy(color = colorResource(R.color.gray_B9B9B9))
                                    )
                                }
                                innerTextField()
                            }
                        },
                        textStyle = textStyle.copy(color = colorResource(R.color.black)),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Text(
                        "소요시간 : ${duration / 60000}분",
                        fontSize = 15.sp,
                        fontFamily = interFontFamily
                    )
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
                            .background(colorResource(R.color.gray_)),
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
                width = 1.dp,
                color = colorResource(R.color.gray_6F6F6F),
                shape = RoundedCornerShape(16.dp)
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
                initHeight = 400,
                isVisible = true,
                content = DriveBottomSheetContent.COURSE_ADD,
                courseAddSheetState = CourseAddScreenState.CourseAddSheetState(
                    isTwoStep = false,
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
                initHeight = 400,
                isVisible = true,
                content = DriveBottomSheetContent.COURSE_ADD,
                courseAddSheetState = CourseAddScreenState.CourseAddSheetState(
                    isTwoStep = true,
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