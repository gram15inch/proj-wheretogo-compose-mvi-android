package com.wheretogo.presentation.composable.content

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim139.core.ui.screen.MapLayoutType
import com.dhkim139.core.ui.screen.rememberContentSize
import com.dhkim139.core.ui.screen.rememberMapLayoutType
import com.dhkim139.core.ui.screen.systemBarPadding
import com.dhkim139.core.ui.screen.thenIf
import com.dhkim139.feature.navigationoption.CourseDirection
import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.NavOption
import com.dhkim139.feature.navigationoption.NavOptionSheet
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.StartDirection
import com.wheretogo.domain.model.map.ContentOperation
import com.wheretogo.domain.model.map.RefreshContentOption
import com.wheretogo.domain.model.util.LatLngLabel
import com.wheretogo.domain.model.util.Navigation
import com.wheretogo.presentation.ExportMap
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.callMap
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.intent.MapIntent
import com.wheretogo.presentation.model.ParkingItem
import com.wheretogo.presentation.viewmodel.DriveViewModel
import com.wheretogo.presentation.viewmodel.MapViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


data class NavOptionArg(val courseId: String, val direction: StartDirection)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavOptionBottomSheet(
    driveViewModel: DriveViewModel,
    mapViewModel: MapViewModel,
    onPaddingUpdate: (Dp, Dp) -> Unit, // bottom, end
    step: DriveTutorialStep = DriveTutorialStep.SKIP,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val arg by driveViewModel.navOptionArg.collectAsStateWithLifecycle()
    var lastArg by remember { mutableStateOf<NavOptionArg?>(null) }
    val state = driveViewModel.driveScreenState.collectAsState()
    val scope = rememberCoroutineScope()
    val isAd = rememberAdVisibility(step)

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val strings = rememberStringResolver()
    val display = rememberMapLayoutType()
    var isOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(sheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            sheetState.partialExpand()
        }
    }

    fun adVisible(boolean: Boolean){
        isAd.visible = boolean
    }

    LaunchedEffect(isOpen, display) {
        when {
            isOpen && (display == MapLayoutType.VERTICAL) -> {
                onPaddingUpdate(300.dp, 0.dp)
            }

            isOpen && (display == MapLayoutType.HORIZONTAL) -> {
                onPaddingUpdate(0.dp, 400.dp)
            }

            !isOpen -> {
                onPaddingUpdate(0.dp, 0.dp)
            }

            else -> {}
        }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .drop(1)
            .collect { value ->
                when (value) {
                    SheetValue.PartiallyExpanded,
                    SheetValue.Hidden -> {
                        driveViewModel.handleIntent(DriveScreenIntent.NavOptionSheetClose)
                        lastArg = null
                    }

                    SheetValue.Expanded -> {
                    }
                }
            }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.targetValue }
            .drop(1)
            .collect { value ->
                when (value) {
                    SheetValue.PartiallyExpanded,
                    SheetValue.Hidden -> {
                        isOpen = false
                        mapViewModel.handleIntent(
                            MapIntent.RefreshContent(
                                option = RefreshContentOption(
                                    ContentOperation.DELETE_PARKING
                                )
                            )
                        )
                    }

                    SheetValue.Expanded -> {
                        isOpen = true
                        adVisible(true)
                    }
                }
            }
    }

    LaunchedEffect(arg) {
        if (arg != null) {
            lastArg = arg
            sheetState.expand()
        }
    }
    val size = rememberContentSize()
    val sheetAlign =
        if (display == MapLayoutType.HORIZONTAL) Alignment.BottomEnd else Alignment.BottomCenter
    val horizontalStartPadding =
        if (display == MapLayoutType.HORIZONTAL) size.width - 400.dp else 0.dp
    val overviewMaxHeight =
        if (display == MapLayoutType.VERTICAL) (size.height - AdMinSize.Card.heightDp.dp).coerceIn(
            100.dp,
            280.dp
        ) else 280.dp
    var contentHeight by remember { mutableStateOf(200.dp) }
    val estimatedSheetHeight = contentHeight + 20.dp

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = sheetAlign) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContent = {
                NavOptionSheet(
                    navOption = lastArg?.toNavOption(),
                    isOpen = isOpen,
                    onFocus = {
                        val item = ParkingItem(LatLng(it.latLng.lat, it.latLng.lng))
                        mapViewModel.handleIntent(MapIntent.ParkingFocus(item))
                    },
                    onStartNavigation = {
                        scope.launch {
                            val ls = it.points.map { lb ->
                                async {
                                    val label = lb.parking?.name ?: strings(lb.role)
                                    LatLngLabel(
                                        latLng = LatLng(
                                            latitude = lb.position.lat,
                                            longitude = lb.position.lng
                                        ),
                                        label = label
                                    )
                                }
                            }.awaitAll()
                            context.callMap(
                                map = ExportMap.NAVER,
                                navigation = Navigation(
                                    courseName = it.courseName,
                                    waypoints = ls,
                                    direction = StartDirection.FORWARD
                                ),
                                startMyLocation = it.useMyLocation
                            )
                        }
                    },
                    onContentHeightChange = {
                        contentHeight = it
                        // 잦은 광고 방지
                        if (contentHeight > overviewMaxHeight)
                            adVisible(false)
                    },
                    overviewMaxHeight = overviewMaxHeight
                )
            },
            sheetContainerColor = BottomSheetDefaults.ContainerColor,
            containerColor = Color.Transparent,
            sheetShadowElevation = 0.dp,
            sheetTonalElevation = 0.dp,
            sheetDragHandle = { DragHandle() },
            modifier = with(modifier) {
                thenIf(
                    condition = display == MapLayoutType.HORIZONTAL,
                    success = {
                        widthIn(max = 400.dp).padding(end = 10.dp)
                    }, fail = {
                        fillMaxWidth()
                    })
            },
        ) { _ ->
            val systemPadding = systemBarPadding()
            SlideAnimation(
                visible = sheetState.targetValue == SheetValue.Expanded && isAd.visible,
                direction = AnimationDirection.RightCenter
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AdaptiveAd(
                        contentPadding = systemPadding.let {
                            it.copy(bottom = it.bottom + estimatedSheetHeight)
                        },
                        sizeMeasurePadding = systemPadding.let {
                            it.copy(
                                start = horizontalStartPadding,
                                bottom = it.bottom + estimatedSheetHeight
                            )
                        },
                        adItem = state.value.searchBarState.adItemGroup.firstOrNull()
                    )
                }
            }
        }
    }
}

private fun NavOptionArg.toNavOption() = NavOption(
    courseId = this.courseId,
    direction = if (this.direction == StartDirection.FORWARD) {
        CourseDirection.FORWARD
    } else {
        CourseDirection.BACKWARD
    }
)

@Composable
private fun rememberStringResolver(
    @StringRes fallbackRes: Int = R.string.unknown,
): (EndpointRole) -> String {
    val resources = LocalResources.current
    val fallback = stringResource(fallbackRes)

    return remember(resources, fallback) {
        { role ->
            runCatching { resources.getString(role.labelRes) }.getOrDefault(fallback)
        }
    }
}

private class AdVisibility(private val step: DriveTutorialStep) {
    private var requested by mutableStateOf(false)
    private val result = derivedStateOf {
        requested && step == DriveTutorialStep.SKIP
    }

    var visible: Boolean
        get() = result.value
        set(value) { requested = value }
}

@Composable
private fun rememberAdVisibility(step: DriveTutorialStep): AdVisibility =
    remember(step) { AdVisibility(step) }