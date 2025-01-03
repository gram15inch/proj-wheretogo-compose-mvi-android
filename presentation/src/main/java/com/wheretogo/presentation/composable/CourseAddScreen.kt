package com.wheretogo.presentation.composable

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation.NavController
import com.naver.maps.map.overlay.Marker
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.DragHandle
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.feature.eventConsumption
import com.wheretogo.presentation.feature.naver.setCurrentLocation
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.state.CourseAddScreenState.RouteDetailItemState
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily
import com.wheretogo.presentation.toStrRes
import com.wheretogo.presentation.viewmodel.CourseAddViewModel
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
fun CourseAddScreen(
    navController: NavController,
    viewModel: CourseAddViewModel = hiltViewModel()
) {
    val state by viewModel.courseAddScreenState.collectAsState()
    val context = LocalContext.current
    var isNotMove by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    if (state.isCourseAddDone) {
        if (isNotMove) {
            navController.navigate("home")
            isNotMove = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        NaverMap(
            modifier = Modifier
                .zIndex(0f)
                .fillMaxSize()
                .height(300.dp)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                .background(color = Color.Green),
            overlayMap = setOf(state.mapOverlay),
            onMapAsync = { map ->
                coroutineScope.launch { map.setCurrentLocation(context) }
            },
            cameraState = state.cameraState,
            onCameraMove = { viewModel.handleIntent(CourseAddIntent.UpdatedCamera(it)) },
            onMapClickListener = { viewModel.handleIntent(CourseAddIntent.MapClick(it)) },
            onCourseMarkerClick = { viewModel.handleIntent(CourseAddIntent.CourseMarkerClick(it as Marker)) },
            contentPadding = context.getMapPadding()
        )
        if (state.isFloatMarker)
            Box(// 중앙 마커
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                )
            }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End
        ) {
            if (state.isFloatingButton) {
                FloatingButtonGroup(
                    onMarkerMoveClick = {
                        viewModel.handleIntent(CourseAddIntent.MarkerMoveFloatingClick)
                    },
                    onMarkerRemoveClick = {
                        viewModel.handleIntent(CourseAddIntent.MarkerRemoveFloatingClick)
                    }
                )
            }
            CourseAddBottomSheet(
                modifier = Modifier,
                courseAddScreenState = state,
                onRouteCreateClick = { viewModel.handleIntent(CourseAddIntent.RouteCreateClick) },
                onRouteDetailItemClick = {
                    viewModel.handleIntent(
                        CourseAddIntent.RouteDetailItemClick(
                            it
                        )
                    )
                },
                onCommendClick = { viewModel.handleIntent(CourseAddIntent.CommendClick) },
                onBackClick = { viewModel.handleIntent(CourseAddIntent.DetailBackClick) },
                onNameEditValueChange = {
                    viewModel.handleIntent(
                        CourseAddIntent.NameEditValueChange(
                            it
                        )
                    )
                }
            )
        }
    }
}


@Preview
@Composable
fun CourseAddScreenPreview() {
    CourseAddBottomSheet(modifier = Modifier, CourseAddScreenState(), {}, {}, {}, {}, {})
}

@Composable
fun FloatingButtonGroup(
    onMarkerMoveClick: () -> Unit,
    onMarkerRemoveClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Green)
                .clickable {
                    onMarkerMoveClick()
                })
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Blue)
                .clickable {
                    onMarkerRemoveClick()
                })
    }
}

@Composable
fun CourseAddBottomSheet(
    modifier: Modifier = Modifier,
    courseAddScreenState: CourseAddScreenState,
    onRouteDetailItemClick: (RouteDetailItemState) -> Unit,
    onRouteCreateClick: () -> Unit,
    onCommendClick: () -> Unit,
    onBackClick: () -> Unit,
    onNameEditValueChange: (String) -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .eventConsumption()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .widthIn(context.getAddPopUpWidthDp())
            .heightIn(context.getAddPopUpHeightDp())
            .background(colorResource(R.color.white))

    ) {
        Column {
            Box(modifier = Modifier.height(340.dp)) {
                DragHandle()
                SlideAnimation(
                    visible = !courseAddScreenState.isDetailContent,
                    direction = AnimationDirection.CenterUp
                ) {
                    RouteWaypointContent(
                        modifier = Modifier
                            .padding(top = 15.dp, start = 15.dp, end = 15.dp, bottom = 5.dp)
                            .fillMaxWidth(),
                        routeName = courseAddScreenState.courseName,
                        duration = courseAddScreenState.duration,
                        waypointItemStateGroup = courseAddScreenState.waypointItemStateGroup,
                        onRouteCreateClick = onRouteCreateClick,
                        onNameEditValueChange = onNameEditValueChange,
                    )
                }

                SlideAnimation(
                    visible = courseAddScreenState.isDetailContent,
                    direction = AnimationDirection.CenterDown
                ) {
                    RouteDetailContent(
                        modifier = Modifier
                            .padding(15.dp)
                            .fillMaxWidth(),
                        routeDetailItemStateGroup = courseAddScreenState.detailItemStateGroup,
                        onRouteDetailItemClick = onRouteDetailItemClick,
                        onBackClick = onBackClick
                    )
                }
            }
            CommendButton(
                isDetailContent = courseAddScreenState.isDetailContent,
                isDone = courseAddScreenState.isCommendActive,
                onCommendClick = onCommendClick
            )
        }
    }
}


@Composable
fun CommendButton(
    isDone: Boolean,
    isDetailContent: Boolean,
    onCommendClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val text = if (isDetailContent) R.string.done else R.string.next_step
        val textColor = if (isDone) R.color.white else R.color.black
        val backColor = if (isDone) R.color.blue else R.color.gray_C7C7C7_80
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorResource(backColor))
                .clickable {
                    onCommendClick()
                },
            contentAlignment = Alignment.Center
        ) {
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
    routeDetailItemStateGroup: List<RouteDetailItemState>,
    onRouteDetailItemClick: (RouteDetailItemState) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Box(modifier = Modifier.clickable(
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
        LazyColumn(
            modifier = Modifier.padding(top = 5.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val detailItemTypeGroup = routeDetailItemStateGroup.groupBy { it.data.type }.toList()

            items(detailItemTypeGroup) { pair ->
                Column {
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
                        text = stringResource(pair.first.toStrRes()),
                        fontSize = 16.sp,
                        fontFamily = interBoldFontFamily
                    )
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(pair.second) { item ->
                            Box(modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    colorResource(if (item.isClick) R.color.blue else R.color.gray_C7C7C7_80)
                                )
                                .clickable {
                                    onRouteDetailItemClick(item)
                                }) {
                                Text(
                                    modifier = Modifier.padding(vertical = 3.dp, horizontal = 8.dp),
                                    text = "${item.data.emogi} ${stringResource(item.data.strRes)}",
                                    fontSize = 11.sp,
                                    fontFamily = interBoldFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

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
    Box(modifier = modifier) {
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
                        .background(colorResource(R.color.gray_C7C7C7_80))
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
            Box(
                modifier = Modifier
                    .padding(top = 15.dp, bottom = 10.dp, start = 10.dp)
                    .fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val textStyle = TextStyle(fontSize = 16.sp, fontFamily = interBoldFontFamily)
                    val focusManager = LocalFocusManager.current
                    BasicTextField(
                        modifier = Modifier.fillMaxWidth(),
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

            FadeAnimation(visible = waypointItemStateGroup.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(waypointItemStateGroup) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .border(
                                    width = 1.dp,
                                    color = colorResource(R.color.gray_808080),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .animateItem()
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
                }
            }
            FadeAnimation(visible = waypointItemStateGroup.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorResource(R.color.gray_)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("생성으로 새 경로를 만들어 보세요.", fontFamily = interFontFamily)
                }
            }


        }
    }
}

@Composable
fun Context.getMapPadding(): ContentPadding {


    val width = resources.displayMetrics.run {
        widthPixels / density
    }
    val height = resources.displayMetrics.run {
        heightPixels / density
    }

    val end = if (width >= 600) getAddPopUpWidthDp() else 0.dp
    val bottom = if (height >= 600) getAddPopUpHeightDp() else 0.dp


    return ContentPadding(
        end = end,
        bottom = bottom
    )
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

fun Context.getAddPopUpHeightDp(): Dp {

    val height = resources.displayMetrics.run {
        heightPixels / density
    }

    val newWidth = if (height >= 600) {
        350f
    } else {
        400f
    }
    return newWidth.dp
}
