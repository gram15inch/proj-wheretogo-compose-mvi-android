package com.wheretogo.presentation.composable

import android.content.Context
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
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.runtime.collectAsState
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
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.model.map.RouteCategory
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.BottomSheet
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.composable.content.SearchBar
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.feature.naver.setCurrentLocation
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily
import com.wheretogo.presentation.toStrRes
import com.wheretogo.presentation.viewmodel.CourseAddViewModel
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Composable
fun CourseAddScreen(
    viewModel: CourseAddViewModel = hiltViewModel()
) {
    val state by viewModel.courseAddScreenState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var bottomSheetHeight by remember { mutableStateOf(0.dp) }
    val mapBottomPadding by animateDpAsState(
        targetValue = bottomSheetHeight,
        animationSpec = tween(durationMillis = 300)
    )

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
        content = { naviBar->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                if (BuildConfig.DEBUG) {
                    Box(
                        modifier = Modifier
                            .systemBarsPadding()
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
                            .zIndex(1f)
                            .padding(bottom = mapBottomPadding + naviBar.calculateBottomPadding()),
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

                SearchBar(
                    modifier = Modifier
                        .zIndex(1f)
                        .statusBarsPadding()
                        .padding(top = 10.dp, end = 10.dp)
                        .align(alignment = Alignment.TopEnd),
                    isLoading = state.searchBarState.isLoading,
                    isEmptyVisible = state.searchBarState.isEmptyVisible,
                    searchBarItemGroup = state.searchBarState.searchBarItemGroup ,
                    onSearchSubmit = { viewModel.handleIntent(CourseAddIntent.SubmitClick(it)) },
                    onSearchBarToggleClick = { viewModel.handleIntent(CourseAddIntent.SearchBarToggleClick(it)) },
                    onSearchBarItemClick = { viewModel.handleIntent(CourseAddIntent.SearchBarItemClick(it)) }
                )
                NaverMap(
                    modifier = Modifier
                        .zIndex(0f)
                        .fillMaxSize()
                        .height(300.dp)
                        .background(color = Color.Green),
                    mapOverlayGroup = state.overlayGroup,
                    onMapAsync = { map ->
                        coroutineScope.launch { map.setCurrentLocation(context) }
                    },
                    cameraState = state.cameraState,
                    onCameraUpdate = { viewModel.handleIntent(CourseAddIntent.CameraUpdated(it)) },
                    onMapClickListener = { viewModel.handleIntent(CourseAddIntent.MapClick(it)) },
                    onCheckPointMarkerClick = { viewModel.handleIntent(CourseAddIntent.WaypointMarkerClick(it)) },
                    contentPadding = ContentPadding(bottom = mapBottomPadding + naviBar.calculateBottomPadding())
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Box {
                    if (state.isFloatingButton) {
                        FloatingButtonGroup(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = mapBottomPadding + naviBar.calculateBottomPadding()),
                            onMarkerMoveClick = {
                                viewModel.handleIntent(CourseAddIntent.MarkerMoveFloatingClick)
                            },
                            onMarkerRemoveClick = {
                                viewModel.handleIntent(CourseAddIntent.MarkerRemoveFloatingClick)
                            }
                        )
                    }
                    BottomSheet(
                        modifier = Modifier,
                        initHeight = 80,
                        isVisible = !state.bottomSheetState.isBottomSheetDown,
                        onHeightChange = { dp ->
                            bottomSheetHeight = dp
                            viewModel.handleIntent(CourseAddIntent.ContentPaddingChanged(dp.value.toInt()))
                        },
                        onStateChange = {
                            viewModel.handleIntent(CourseAddIntent.SheetStateChange(it))
                        }
                    ) {
                        CourseAddContent(
                            state = state.bottomSheetState.courseAddState,
                            onRouteCreateClick = { viewModel.handleIntent(CourseAddIntent.RouteCreateClick) },
                            onCategorySelect = { viewModel.handleIntent(CourseAddIntent.RouteCategorySelect(it)) },

                            onCommendClick = { viewModel.handleIntent(CourseAddIntent.CommendClick) },
                            onBackClick = { viewModel.handleIntent(CourseAddIntent.DetailBackClick) },
                            onNameEditValueChange = { viewModel.handleIntent(CourseAddIntent.NameEditValueChange(it)) },
                        )

                    }
                }
            }
        }
    )

}


@Preview
@Composable
fun CourseAddScreenPreview() {
    BottomSheet(
        modifier = Modifier.height(400.dp),
        400,
        true,
        {},
        {}
    ) {
        CourseAddContent(
            CourseAddScreenState.CourseAddState(
                isTwoStep = true,
                selectedCategoryCodeGroup = mapOf(
                    RouteAttr.TYPE to 1,
                    RouteAttr.LEVEL to 4,
                    RouteAttr.RELATION to 9,
                )
            ),
            {},
            {},
            {},
            {},
            {},
        )
    }
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
fun CourseAddContent(
    state: CourseAddScreenState.CourseAddState,
    onCategorySelect: (RouteCategory) -> Unit,
    onRouteCreateClick: () -> Unit,
    onCommendClick: () -> Unit,
    onNameEditValueChange: (String) -> Unit,
    onBackClick: () -> Unit,
) {
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

@Composable
fun CommendButton(
    modifier: Modifier,
    isLoading: Boolean,
    isDone: Boolean,
    isDetailContent: Boolean,
    onCommendClick: () -> Unit
) {
    Box(
        modifier = modifier.padding(horizontal = 15.dp, vertical = 5.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val text = if (isDetailContent) R.string.done else R.string.next_step
        val textColor = if (isDone) R.color.white else R.color.black
        val backColor = if (isDone) R.color.blue else R.color.white
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
                .clickable {
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
                        .clickable { onRouteCreateClick() },
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

fun Context.getAddPopUpWidthDp(): Dp {
    val width = resources.displayMetrics.run {
        widthPixels / density
    }


    val newWidth = if (width >= 600) {
        min(350f, width / 2)
    } else {
        400f
    }
    return newWidth.dp
}
